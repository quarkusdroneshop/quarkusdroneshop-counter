# quarkusdroneshop-counter

Quarkus ベースの注文調整マイクロサービス。Web サービスから受け取った注文を PostgreSQL に記録し、QDCA10・QDCA10Pro の各ドリンク製造マイクロサービスへルーティングします。製造完了イベントを受け取り、Web サービスへ結果を通知します。

- **バージョン**: 5.2.1
- **Quarkus**: 3.36.3

## アーキテクチャ

```
quarkusdroneshop-web
    │  orders-in ──▶
    ▼
quarkusdroneshop-counter ──▶ qdca10-in ──▶ quarkusdroneshop-qdca10
                         ──▶ qdca10pro-in ──▶ quarkusdroneshop-qdca10pro
                         ◀── orders-up (shop-bsite.orders-up in prod)
                         ──▶ web-updates ──▶ quarkusdroneshop-web
                    ──PostgreSQL──▶ droneshopdb
```

## Kafka トピック

| チャネル | dev トピック | prod トピック | 方向 |
|---|---|---|---|
| orders-in | `orders-in` | `orders-in` | 受信 |
| orders-up | `orders-up` | `shop-bsite.orders-up` | 受信 |
| qdca10 | `qdca10-in` | `qdca10-in` | 送信 |
| qdca10pro | `qdca10pro-in` | `qdca10pro-in` | 送信 |
| web-updates | `web-updates` | `web-updates` | 送信 |

## ローカル開発

```shell
git clone https://github.com/quarkusdroneshop/quarkusdroneshop-support.git
cd quarkusdroneshop-support
docker compose up

cd ../quarkusdroneshop-counter
./mvnw clean compile quarkus:dev
```

## Kafka トピック確認

```shell
kafka-console-consumer --bootstrap-server localhost:9092 --topic orders-in --from-beginning
kafka-console-consumer --bootstrap-server localhost:9092 --topic web-updates --from-beginning
kafka-console-consumer --bootstrap-server localhost:9092 --topic qdca10-in --from-beginning
kafka-console-consumer --bootstrap-server localhost:9092 --topic qdca10pro-in --from-beginning
kafka-console-consumer --bootstrap-server localhost:9092 --topic orders-up --from-beginning
```

## 環境変数 (本番)

| 変数名 | 説明 |
|---|---|
| `KAFKA_BOOTSTRAP_URLS` | Kafka ブローカー URL |
| `POSTGRESQL_JDBC_URL` | PostgreSQL JDBC URL |
| `POSTGRESQL_USER` | DB ユーザー名 |
| `POSTGRESQL_PASSWORD` | DB パスワード |

## パッケージング

```shell
# ネイティブビルド
./mvnw clean package -Pnative -Dquarkus.native.container-build=true

# Docker イメージ作成
docker build -f src/main/docker/Dockerfile.native -t <REGISTRY>/quarkusdroneshop-counter .

# 実行
docker run -i --network="host" \
  -e KAFKA_BOOTSTRAP_URLS=localhost:9092 \
  -e POSTGRESQL_JDBC_URL="jdbc:postgresql://localhost:5432/droneshopdb?currentSchema=droneshop" \
  -e POSTGRESQL_USER=droneshopuser \
  -e POSTGRESQL_PASSWORD=redhat-21 \
  <REGISTRY>/quarkusdroneshop-counter:latest
```

## 参考

- [Quarkus](https://quarkus.io/)
- [quarkusdroneshop.github.io](https://quarkusdroneshop.github.io)
