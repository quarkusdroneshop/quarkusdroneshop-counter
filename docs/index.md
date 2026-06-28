# Counter マイクロサービス

## 概要

Counter はドローンショップの **注文調整マイクロサービス** です。システム全体の中心的な役割を担い、以下を処理します。

- Web サービスから Kafka 経由で注文を受信
- 注文を PostgreSQL データベースに記録
- QDCA10・QDCA10Pro マイクロサービスへ製造指示を送信
- 各サービスからの完了通知を受信
- Web サービスへステータス更新を送信

**フレームワーク**: Quarkus（Supersonic Subatomic Java Framework）  
**デプロイ先クラスター**: a-cluster

---

## アーキテクチャ

```
Web サービス
    │
    ▼ Kafka: orders-in
┌─────────┐     PostgreSQL
│ Counter │ ──► droneshopdb
└─────────┘
    │
    ├──► Kafka: qdca10-in    ──► QDCA10
    ├──► Kafka: qdca10pro-in ──► QDCA10Pro
    │
    ◄── Kafka: orders-up（QDCA10/Pro からの完了通知）
    │
    ▼ Kafka: web-updates
Web サービス（ステータス更新）
```

### Kafka トピック一覧

| トピック | 方向 | 説明 |
|---------|------|------|
| `orders-in` | 受信 | Web からの新規注文 |
| `qdca10-in` | 送信 | QDCA10 への製造指示 |
| `qdca10pro-in` | 送信 | QDCA10Pro への製造指示 |
| `orders-up` / `shop-bsite-orders-up` | 受信 | 製造完了通知 |
| `web-updates` | 送信 | フロントへのステータス更新 |

### 依存サービス

- **PostgreSQL** (droneshopdb): 注文データの永続化
- **Apache Kafka**: 全メッセージングバス
- **quarkusdroneshop-qdca10**: ドローンA製造サービス
- **quarkusdroneshop-qdca10pro**: ドローンB製造サービス
- **quarkusdroneshop-inventory**: 在庫管理サービス

---

## ローカル開発

### 前提条件

- Java 17+ （[SDKMan](https://sdkman.io/) での管理推奨）
- Docker / Docker Compose

### 1. インフラ起動

```shell
git clone https://github.com/quarkusdroneshop/quarkusdroneshop-support.git
cd quarkusdroneshop-support
docker compose up -d
```

PostgreSQL・Kafka・Zookeeper が起動します。

### 2. アプリケーション起動

```shell
git clone https://github.com/quarkusdroneshop/quarkusdroneshop-counter.git
cd quarkusdroneshop-counter
./mvnw clean compile quarkus:dev
```

Dev UI: http://localhost:8080/q/dev

### 3. Kafka トピック監視

```shell
# 注文の受信確認
kafka-console-consumer --bootstrap-server localhost:9092 --topic orders-in --from-beginning

# Web へのステータス更新確認
kafka-console-consumer --bootstrap-server localhost:9092 --topic web-updates --from-beginning

# 製造指示確認
kafka-console-consumer --bootstrap-server localhost:9092 --topic qdca10-in --from-beginning
kafka-console-consumer --bootstrap-server localhost:9092 --topic qdca10pro-in --from-beginning
```

### 環境変数

| 変数名 | デフォルト | 説明 |
|--------|-----------|------|
| `KAFKA_BOOTSTRAP_URLS` | `localhost:9092` | Kafka ブートストラップアドレス |
| `PGSQL_URL` | `jdbc:postgresql://localhost:5432/droneshopdb?currentSchema=droneshop` | DB 接続 URL |
| `PGSQL_USER` | `droneshopuser` | DB ユーザー名 |
| `PGSQL_PASS` | `redhat-21` | DB パスワード |

---

## 本番デプロイ（Tekton Pipeline）

### パイプライン概要

RHDH の **CI タブ** からパイプラインを確認・実行できます。

```
fetch-repository → semgrep-scan → maven-run → push-oc-apps
```

| ステップ | 内容 |
|---------|------|
| `fetch-repository` | GitHub からソースをクローン |
| `semgrep-scan` | SAST セキュリティスキャン（p/java, p/owasp-top-ten, p/secrets） |
| `maven-run` | `clean verify -Dquarkus.package.jar.type=uber-jar` |
| `push-oc-apps` | OpenShift へビルド＆デプロイ |

### 手動実行

```shell
# Tekton CLI でパイプライン実行
tkn pipeline start build-and-push-quarkusdroneshop-counter \
  -n quarkusdroneshop-cicd \
  --use-param-defaults
```

### ArgoCD による GitOps デプロイ

本番環境への最終デプロイは ArgoCD が自動同期します。  
RHDH の **CD タブ** で ArgoCD アプリの状態を確認できます。

---

## テスト

```shell
# ユニットテスト(ArchUnit含む)
./mvnw test

# 統合テスト（Jacoco含む）
./mvnw verify

# チェックスタイル
./mvnw checkstyle:check

# PMD
./mvnw pmd:pmd

# SpotBugs
./mvnw spotbugs:spotbugs

# semgrep
semgrep scan --config p/default --json > target/semgrep-results.json

# secret scan
gitleaks detect --source . --report-format json --report-path target/gitleaks-report.json --exit-code 1

# 脆弱性テスト
trivy fs --scanners vuln,secret,misconfig,license --exit-code=1 --ignorefile ./.trivyignore.yaml ./ > target/trivy.txt

# セキュリティテスト
mvn quarkus:dev > quarkus.log 2>&1 & QUARKUS_PID=$!; sleep 10; wapiti -u http://localhost:8080 -f json -o ./target/wapiti.json; kill $QUARKUS_PID

# テストレポートの作成
./mvnw exec:exec@generate-report
```

### 手動テスト（ローカル）

```shell
# 注文送信
curl -X POST http://localhost:8080/order \
  -H "Content-Type: application/json" \
  -d '{"item": "DRONE_A", "quantity": 1, "location": "HOME"}'
```

---

## 注意事項

- **データベーススキーマ**: `droneshop` スキーマを使用。Flyway で自動マイグレーション。
- **Kafka メッセージ形式**: JSON シリアライズ。`OrderTicket` POJO を参照。
- **冪等性**: 同一 `orderId` の重複処理に注意。Counter は orderId で重複チェックを行います。
- **b-cluster との通信**: Kafka MirrorMaker2 経由でクラスター間トピックを同期。`shop-bsite-*` プレフィックスのトピックは b-cluster からのミラーリング。
