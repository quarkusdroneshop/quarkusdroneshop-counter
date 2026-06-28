package io.quarkusdroneshop;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * ArchUnit によるアーキテクチャ適合性テスト。
 * パッケージ構造:
 *   io.quarkusdroneshop.counter.domain.*    - ドメイン層 (エンティティ / イベント / 値オブジェクト / コマンド)
 *   io.quarkusdroneshop.infrastructure.*    - インフラ層 (Kafka / シリアライズ)
 */
@AnalyzeClasses(
        packages = "io.quarkusdroneshop",
        importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    // =========================================================================
    // 1. 命名規則
    // =========================================================================

    @ArchTest
    static final ArchRule Deserializer命名規則 =
        classes()
            .that().implement("org.apache.kafka.common.serialization.Deserializer")
            .or().areAssignableTo(
                io.quarkus.kafka.client.serialization.ObjectMapperDeserializer.class)
            .should().haveSimpleNameEndingWith("Deserializer");

    @ArchTest
    static final ArchRule 例外クラスの命名規則 =
        classes()
            .that().areAssignableTo(Exception.class)
            .and().resideInAPackage("io.quarkusdroneshop..")
            .should().haveSimpleNameEndingWith("Exception");

    // =========================================================================
    // 2. パッケージ配置ルール
    // =========================================================================

    @ArchTest
    static final ArchRule Deserializerはinfrastructureに配置 =
        classes()
            .that().haveSimpleNameEndingWith("Deserializer")
            .should().resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule コマンドはCommandsパッケージに配置 =
        classes()
            .that().haveSimpleNameEndingWith("Command")
            .should().resideInAPackage("..commands..");

    // =========================================================================
    // 3. レイヤー間依存ルール
    // =========================================================================

    @ArchTest
    static final ArchRule ドメイン層はJAX_RSを使用しない =
        noClasses()
            .that().resideInAPackage("io.quarkusdroneshop.counter.domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("jakarta.ws.rs..");

    @ArchTest
    static final ArchRule コマンドはInfrastructureに依存しない =
        noClasses()
            .that().resideInAPackage("..commands..")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule ドメインクラスはPublic =
        classes()
            .that().resideInAPackage("io.quarkusdroneshop.counter.domain")
            .and().areNotInterfaces()
            .should().bePublic();

    @ArchTest
    static final ArchRule Infrastructureの依存範囲チェック =
        classes()
            .that().resideInAPackage("io.quarkusdroneshop.infrastructure..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                "io.quarkusdroneshop.infrastructure..",
                "io.quarkusdroneshop.counter.domain..",
                "io.quarkusdroneshop.domain..",
                "java..",
                "javax..",
                "jakarta..",
                "io.quarkus..",
                "io.smallrye..",
                "org.eclipse.microprofile..",
                "org.apache.kafka..",
                "com.fasterxml..",
                "org.slf4j..",
                "org.jboss..",
                "io.debezium..");

    // =========================================================================
    // 4. 循環依存
    // =========================================================================

    @ArchTest
    static final ArchRule パッケージ間循環依存なし =
        slices()
            .matching("io.quarkusdroneshop.(*)..")
            .should().beFreeOfCycles();
}
