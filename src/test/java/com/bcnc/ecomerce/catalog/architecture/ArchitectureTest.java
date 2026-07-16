package com.bcnc.ecomerce.catalog.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private final JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
            .importPackages("com.bcnc.ecomerce.catalog");

    @Test
    void controllers_should_only_depend_on_use_cases() {
        ArchRule rule = classes()
                .that().resideInAPackage("..adapter.in.rest..")
                .should().onlyDependOnClassesThat(
                        resideInAPackage("..application..")
                                .or(resideInAPackage("..domain.."))
                                .or(resideInAPackage("..adapter.in.rest.."))
                                .or(resideInAPackage("java.."))
                                .or(resideInAPackage("jakarta.."))
                                .or(resideInAPackage("com.fasterxml.."))
                                .or(resideInAPackage("io.swagger.."))
                                .or(resideInAPackage("org.springframework.."))
                                .or(resideInAPackage("org.springdoc.."))
                );

        rule.check(importedClasses);
    }

    @Test
    void services_should_only_depend_on_ports_and_domain() {
        ArchRule rule = classes()
                .that().resideInAPackage("..application.service..")
                .should().onlyDependOnClassesThat(
                        resideInAPackage("..application..")
                                .or(resideInAPackage("..domain.."))
                                .or(resideInAPackage("java.."))
                                .or(resideInAPackage("org.springframework.."))
                );

        rule.check(importedClasses);
    }

    @Test
    void domain_should_not_depend_on_frameworks() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat(
                        resideInAPackage("org.springframework..")
                                .or(resideInAPackage("jakarta.."))
                                .or(resideInAPackage("javax.."))
                );

        rule.check(importedClasses);
    }

    @Test
    void persistence_adapters_should_not_depend_on_rest_adapters() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..adapter.out.persistence..")
                .should().dependOnClassesThat(
                        resideInAPackage("..adapter.in.rest..")
                );

        rule.check(importedClasses);
    }

    @Test
    void security_adapters_should_not_depend_on_rest_adapters() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..adapter.out.security..")
                .should().dependOnClassesThat(
                        resideInAPackage("..adapter.in.rest..")
                );

        rule.check(importedClasses);
    }

    @Test
    void ports_should_not_depend_on_adapters() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..application.port..")
                .should().dependOnClassesThat(
                        resideInAPackage("..adapter..")
                );

        rule.check(importedClasses);
    }

    @Test
    void dtos_should_reside_in_application_package() {
        ArchRule rule = classes()
                .that().resideInAPackage("..dto..")
                .should().resideInAPackage("..application..");

        rule.check(importedClasses);
    }

    @Test
    void entities_should_reside_in_adapter_package() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*Entity")
                .should().resideInAPackage("..adapter.out.persistence.entity..");

        rule.check(importedClasses);
    }

    @Test
    void repositories_should_reside_in_adapter_package() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*Repository")
                .and().haveNameNotMatching(".*Port")
                .should().resideInAPackage("..adapter.out.persistence..");

        rule.check(importedClasses);
    }
}
