package io.github.qudtlib.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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

    @Override public Map<String, Unit> loadUnits() {
        Map units = new HashMap<>();
        <#list units as iri, unit>
        addUnit${iri?index?c}(units);
        </#list>
        return units;
    }

    <#list units as iri, unit>
    private static void addUnit${iri?index?c}(Map<String, Unit> units) {
        Unit unit = null;
        unit = new Unit(${q(iri)}, ${optStr(unit.prefixIri)}, ${optStr(unit.scalingOfIri)}, ${optStr(unit.dimensionVectorIri)}, ${optBigDec(unit.conversionMultiplier)}, ${optBigDec(unit.conversionOffset)}, ${optStr(unit.symbol)});
        <#list unit.labels as label>
        unit.addLabel(new LangString(${q(label.string)}, ${optStr(label.languageTag)}));
        </#list>
        <#list unit.quantityKindIris as quantityKindIri>
        unit.addQuantityKind(${q(quantityKindIri)});
        </#list>
        units.put(${q(iri)}, unit);
    }
    </#list>

    @Override public Map<String, QuantityKind> loadQuantityKinds() {
        Map quantityKinds = new HashMap<>();
        <#list quantityKinds as iri, quantityKind>
        addQuantityKind${iri?index?c}(quantityKinds);
        </#list>
        return quantityKinds;
    }

    <#list quantityKinds as iri, quantityKind>
    private static void addQuantityKind${iri?index?c}(Map<String, QuantityKind> quantityKinds){
        QuantityKind quantityKind = new QuantityKind(${q(iri)}, ${optStr(quantityKind.dimensionVector)}, ${optStr(quantityKind.symbol)});
        <#list quantityKind.labels as label>
        quantityKind.addLabel(new LangString(${q(label.string)}, ${optStr(label.languageTag)}));
        </#list>
        <#list quantityKind.applicableUnits as unitIri>
        quantityKind.addApplicableUnit(${q(unitIri)});
        </#list>
        <#list quantityKind.broaderQuantityKinds as qkIri>
            quantityKind.addBroaderQuantityKind(${q(qkIri)});
        </#list>
        quantityKinds.put(${q(iri)}, quantityKind);
    }
    </#list>

    @Override public Map<String, Prefix> loadPrefixes() {
        Map prefixes = new HashMap<>();
        Prefix prefix = null;
        <#list prefixes as iri, prefix>
        prefix = new Prefix(${q(iri)}, ${bigDec(prefix.multiplier)}, ${q(prefix.symbol)}, (String) ${nullableStr(prefix.ucumCode)});
            <#list prefix.labels as label>
            prefix.addLabel(new LangString(${q(label.string)}, ${optStr(label.languageTag)}));
            </#list>
            prefixes.put(${q(iri)}, prefix);
        </#list>
        return prefixes;
    }

    @Override public void loadFactorUnits(Map<String, Unit> units) {
        <#list units as iri, unit>
            <#if unit.hasFactorUnits()>
            setFactorUnits${iri?counter?c}(units);
            </#if>
        </#list>
    }

    <#list units as iri, unit>
        <#if unit.hasFactorUnits()>
        private static void setFactorUnits${iri?counter?c}(Map<String, Unit> units){
            Unit unit = units.get("${iri}");
            <#list unit.factorUnits as factorUnit>
            unit.addFactorUnit(new FactorUnit(units.get("${factorUnit.unit.iri}"), ${factorUnit.exponent}));
            </#list>
        }
        </#if>
    </#list>
}
