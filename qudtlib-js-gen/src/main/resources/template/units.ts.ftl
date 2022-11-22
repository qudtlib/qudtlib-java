/**
 * Code generated by qudtlib-java:qudtlib-js-gen.
 */
import { Decimal } from "decimal.js";
import {
  config,
  Unit,
  QuantityKind,
  Prefix,
  LangString,
  FactorUnit,
  Qudt,
} from "@qudtlib/core";

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
{
  let unit: Unit;
<#list units as iri, unit>
  unit = new Unit(
    ${q(iri)},
    undefined,
    ${optStr(unit.dimensionVectorIri)},
    ${optBigDec(unit.conversionMultiplier)},
    ${optBigDec(unit.conversionOffset)},
    ${optStr(unit.prefixIri)},
    ${optStr(unit.scalingOfIri)},
    undefined,
    ${optStr(unit.symbol)},
    undefined,
    ${optStr(unit.currencyCode)},
    ${optNum(unit.currencyNumber)}
  );
    <#list unit.labels as label>
  unit.addLabel(new LangString(${q(label.string)}, ${optStr(label.languageTag)}));
    </#list>
    <#list unit.quantityKindIris as quantityKindIri>
  unit.addQuantityKindIri(
    ${q(quantityKindIri)}
  );
    </#list>
  config.units.set(${q(iri)}, unit);
</#list>
}

export const Units = {
<#list unitConstants as u>
  // ${u.label}
  ${u.codeConstantName}: Qudt.unitFromLocalnameRequired("${u.iriLocalname}"),
</#list>
}

// QuantityKinds
{
  let quantityKind: QuantityKind;
<#list quantityKinds as iri, quantityKind>
  quantityKind = new QuantityKind(${q(iri)}, ${optStr(quantityKind.dimensionVectorIri)}, ${optStr(quantityKind.symbol)}, undefined);
    <#list quantityKind.labels as label>
  quantityKind.addLabel(new LangString(${q(label.string)}, ${optStr(label.languageTag)}));
    </#list>
    <#list quantityKind.applicableUnitIris as unitIri>
  quantityKind.addApplicableUnitIri(${q(unitIri)});
    </#list>
    <#list quantityKind.broaderQuantityKindIris as qkIri>
  quantityKind.addBroaderQuantityKindIri(${q(qkIri)});
    </#list>
  config.quantityKinds.set(${q(iri)}, quantityKind);
</#list>
}

export const QuantityKinds = {
<#list quantityKindConstants as q>
  // ${q.label}
  ${q.codeConstantName}: Qudt.quantityKindFromLocalnameRequired("${q.iriLocalname}"),
</#list>
}

// Prefixes
{
  let prefix: Prefix;
<#list prefixes as iri, prefix>
  prefix = new Prefix(${q(iri)}, ${bigDec(prefix.multiplier)}, ${q(prefix.symbol)}, ${optStr(prefix.ucumCode)}, undefined);
    <#list prefix.labels as label>
  prefix.addLabel(new LangString(${q(label.string)}, ${optStr(label.languageTag)}));
    </#list>
  config.prefixes.set(${q(iri)}, prefix);
</#list>
}

export const Prefixes = {
<#list prefixConstants as q>
  // ${q.label}
  ${q.codeConstantName}: Qudt.prefixFromLocalnameRequired("${q.iriLocalname}"),
</#list>
}

function getPrefix(iri:string): Prefix {
  const prefix: Prefix | undefined = config.prefixes.get(iri);
  if (!prefix) {
    throw `prefix ${r"${iri}"} referenced but not loaded`;
  }
  return prefix;
}

function getUnit(iri:string): Unit {
  const unit: Unit | undefined = config.units.get(iri);
  if (!unit) {
    throw `unit ${r"${iri}"} referenced but not loaded`;
  }
  return unit;
}

function getQuantityKind(iri:string): QuantityKind {
  const quantityKind: QuantityKind | undefined = config.quantityKinds.get(iri);
  if (!quantityKind) {
    throw `quantityKind ${r"${iri}"} referenced but not loaded`;
  }
  return quantityKind;
}

// Connect objects
for (const unit of config.units.values()){
  !!unit.prefixIri && unit.setPrefix(getPrefix(unit.prefixIri));
  !!unit.scalingOfIri && unit.setScalingOf(getUnit(unit.scalingOfIri));
  for (const qkIri of unit.quantityKindIris){
    unit.addQuantityKind(getQuantityKind(qkIri));
  }
}

// Set factor units

{
  let unit:Unit;
<#list units as iri, unit>
    <#if unit.hasFactorUnits()>
  unit = getUnit("${iri}");
        <#list unit.factorUnits as factorUnit>
  unit.addFactorUnit(new FactorUnit(getUnit("${factorUnit.unit.iri}"), ${factorUnit.exponent}));
        </#list>
    </#if>
</#list>
}
