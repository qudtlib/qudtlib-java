package io.github.qudtlib.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

<#-- null constant -->
<#assign null = "null" >
<#-- quote the value str with double quotes and escape its contents for insertion in a java string-->
<#function q str>
    <#return "\"" + str?j_string + "\"" >
</#function>
<#-- optional String -->
<#function optStr optVal>
    <#return optVal.isPresent()?then(q(optVal.get()), null) >
</#function>
<#-- string property of optional value-->
<#function optValIri optVal>
    <#return optVal.isPresent()?then(q(optVal.get().iri), null) >
</#function>


<#-- nullable String -->
<#function nullableStr nVal="">
    <#if nVal?has_content>
        <#return q(nVal) >
    <#else >
        <#return null >
    </#if>
</#function>

<#-- optional numeric literal, e.g. Optional<Double> -->
<#function optNum optVal>
    <#return optVal.isPresent()?then(optVal.get()?string.computer, null) >
</#function>

<#-- non-nullable bigdecimal -->
<#function bigDec val>
    <#return "new BigDecimal(\"" + val?string.@toString + "\")" >
</#function>

<#-- optional numeric literal, e.g. Optional<Double> -->
<#function optBigDec optVal>
    <#return optVal.isPresent()?then(bigDec(optVal.get()), null) >
</#function>

public class InitializerImpl implements Initializer {

        @Override
        public Definitions loadData() {
            Definitions definitions = new Definitions();
            populateUnitDefinitions(definitions);
            populateQuantityKindDefinitions(definitions);
            populatePrefixDefinitions(definitions);
            populateFactorUnits(definitions);
            populateSystemOfUnitsDefinitions(definitions);
            return definitions;
        }

    private void populateUnitDefinitions(Definitions definitions) {
        <#list units as iri, unit>
        addUnit${iri?index?c}(definitions);
        </#list>
    }

    <#list units as iri, unit>
    private static void addUnit${iri?index?c}(Definitions definitions) {
        Unit.Definition def = Unit
            .definition(${q(iri)})
            .prefix(definitions.expectPrefixDefinition(${optValIri(unit.prefix)}))
            .scalingOf(definitions.expectUnitDefinition(${optValIri(unit.scalingOf)}))
            .dimensionVectorIri(${optStr(unit.dimensionVectorIri)})
            .conversionMultiplier(${optBigDec(unit.conversionMultiplier)})
            .conversionOffset(${optBigDec(unit.conversionOffset)})
            .symbol(${optStr(unit.symbol)})
            .currencyCode(${optStr(unit.currencyCode)})
            .currencyNumber(${optNum(unit.currencyNumber)})
            <#list unit.labels as label>
            .addLabel(new LangString(${q(label.string)}, ${optStr(label.languageTag)}))
            </#list>
            <#list unit.quantityKinds as quantityKind>
            .addQuantityKind(definitions.expectQuantityKindDefinition(${q(quantityKind.iri)}))
            </#list>
            <#list unit.unitOfSystems as sou>
            .addUnitOfSystem(definitions.expectSystemOfUnitsDefinition(${q(sou.iri)}))
            </#list>
            ;
        definitions.addUnitDefinition(def);
    }
    </#list>

    private void populateQuantityKindDefinitions(Definitions definitions) {
        <#list quantityKinds as iri, quantityKind>
        addQuantityKind${iri?index?c}(definitions);
        </#list>
    }

    <#list quantityKinds as iri, quantityKind>
    private static void addQuantityKind${iri?index?c}(Definitions definitions){
        QuantityKind.Definition def =
            QuantityKind
                .definition(${q(iri)})
                .dimensionVectorIri(${optStr(quantityKind.dimensionVectorIri)})
                .symbol(${optStr(quantityKind.symbol)})
                <#list quantityKind.labels as label>
                .addLabel(new LangString(${q(label.string)}, ${optStr(label.languageTag)}))
                </#list>
                <#list quantityKind.applicableUnits as unit>
                .addApplicableUnit(definitions.expectUnitDefinition(${q(unit.iri)}))
                </#list>
                <#list quantityKind.broaderQuantityKinds as qk>
                .addBroaderQuantityKind(definitions.expectQuantityKindDefinition(${q(qk.iri)}))
                </#list>
                ;
        definitions.addQuantityKindDefinition(def);
    }
    </#list>

    private void populatePrefixDefinitions(Definitions definitions) {
        Prefix.Definition def =  null;
        <#list prefixes as iri, prefix>
            def = Prefix
                    .definition(${q(iri)})
                    .multiplier(${bigDec(prefix.multiplier)})
                    .symbol(${q(prefix.symbol)})
                    .ucumCode((String) ${nullableStr(prefix.ucumCode)})
                    <#list prefix.labels as label>
                    .addLabel(new LangString(${q(label.string)}, ${optStr(label.languageTag)}))
                    </#list>
                    ;
            definitions.addPrefixDefinition(def);
        </#list>
    }

    private void populateFactorUnits(Definitions definitions) {
        <#list units as iri, unit>
            <#if unit.hasFactorUnits()>
            setFactorUnits${iri?counter?c}(definitions);
            </#if>
        </#list>
    }

    private static Supplier<? extends RuntimeException> exceptionSupplier(String iri){
        return () -> new IllegalStateException("Not found: " + iri);
    }

    <#list units as iri, unit>
        <#if unit.hasFactorUnits()>
        private static void setFactorUnits${iri?counter?c}(Definitions definitions){
            String iri = "${iri}";
            Unit.Definition def =
                definitions.getUnitDefinition(iri).orElseThrow(exceptionSupplier(iri))
                <#list unit.factorUnits as factorUnit>
                .addFactorUnit(
                    FactorUnit
                        .builder()
                        .unit(definitions.expectUnitDefinition("${factorUnit.unit.iri}"))
                        .exponent(${factorUnit.exponent})
                )
                </#list>
                ;
        }
        </#if>
    </#list>

    private void populateSystemOfUnitsDefinitions(Definitions definitions) {
        SystemOfUnits.Definition def =  null;
        <#list systemsOfUnits as iri, sou>
            def = SystemOfUnits
                .definition(${q(iri)})
                .abbreviation(${nullableStr(sou.abbreviation)})
                <#list sou.labels as label>
                .addLabel(new LangString(${q(label.string)}, ${optStr(label.languageTag)}))
                </#list>
                <#list sou.baseUnits as baseUnit>
                .addBaseUnit(definitions.expectUnitDefinition(${q(baseUnit.iri)}))
                </#list>
                ;
            definitions.addSystemOfUnitsDefinition(def);
        </#list>
    }
}

