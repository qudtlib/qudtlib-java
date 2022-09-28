import { Decimal } from "decimal.js";
import { config, QudtlibConfig, Unit, QuantityKind, Prefix, LangString, FactorUnit, Qudt } from "@qudtlib/core";

export * from "@qudtlib/core";
<#-- null constant -->
<#assign null = "undefined" >
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
    <#return "new Decimal(\"" + val?string.@toString + "\")" >
</#function>
<#-- optional numeric literal, e.g. Optional<Double> -->
<#function optBigDec optVal>
    <#return optVal.isPresent()?then(bigDec(optVal.get()), null) >
</#function>
// Units
<#list units as iri, unit>
function addUnit${iri?index?c}(config: QudtlibConfig) {
    let unit: Unit;
    unit = new Unit(${q(iri)}, undefined, ${optStr(unit.dimensionVectorIri)}, ${optBigDec(unit.conversionMultiplier)}, ${optBigDec(unit.conversionOffset)}, ${optStr(unit.prefixIri)}, ${optStr(unit.scalingOfIri)}, undefined, ${optStr(unit.symbol)}, undefined);
    <#list unit.labels as label>
    unit.addLabel(new LangString(${q(label.string)}, ${optStr(label.languageTag)}));
    </#list>
    <#list unit.quantityKindIris as quantityKindIri>
    unit.addQuantityKindIri(${q(quantityKindIri)});
    </#list>
    config.units.set(${q(iri)}, unit);
}
</#list>

<#list units as iri, unit>
addUnit${iri?index?c}(config);
</#list>

export const Units = {
<#list unitConstants as u>
    // ${u.label}
    ${u.codeConstantName}: Qudt.unitFromLocalname("${u.iriLocalname}"),
</#list>
}

// QuantityKinds
<#list quantityKinds as iri, quantityKind>
function addQuantityKind${iri?index?c}(config: QudtlibConfig) {
    let quantityKind: QuantityKind;
    quantityKind = new QuantityKind(${q(iri)}, ${optStr(quantityKind.dimensionVectorIri)}, ${optStr(quantityKind.symbol)}, undefined);
    <#list quantityKind.labels as label>
        quantityKind.addLabel(new LangString(${q(label.string)}, ${optStr(label.languageTag)}));
    </#list>
    config.quantityKinds.set(${q(iri)}, quantityKind);
}
</#list>

<#list quantityKinds as iri, quantityKind>
addQuantityKind${iri?index?c}(config);
</#list>

export const QuantityKinds = {
<#list quantityKindConstants as q>
    // ${q.label}
    ${q.codeConstantName}: Qudt.quantityKindFromLocalname("${q.iriLocalname}"),
</#list>
}

// Prefixes
<#list prefixes as iri, prefix>
function addPrefix${iri?index?c}(config: QudtlibConfig) {
    let prefix: Prefix;
    prefix = new Prefix(${q(iri)}, ${bigDec(prefix.multiplier)}, ${q(prefix.symbol)}, ${optStr(prefix.ucumCode)}, undefined);
    <#list prefix.labels as label>
        prefix.addLabel(new LangString(${q(label.string)}, ${optStr(label.languageTag)}));
    </#list>
    config.prefixes.set(${q(iri)}, prefix);
}
</#list>

<#list prefixes as iri, prefix>
addPrefix${iri?index?c}(config);
</#list>

export const Prefixes = {
<#list prefixConstants as q>
    // ${q.label}
    ${q.codeConstantName}: Qudt.prefixFromLocalname("${q.iriLocalname}"),
</#list>
}