import { Decimal } from "decimal.js";
import { config, Configurator, Unit, LangString, FactorUnit } from "@qudtlib/core";


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
    <#return "new Decimal(\"" + val?string.@toString + "\")" >
</#function>

<#-- optional numeric literal, e.g. Optional<Double> -->
<#function optBigDec optVal>
    <#return optVal.isPresent()?then(bigDec(optVal.get()), null) >
</#function>





    <#list units as iri, unit>
    function addUnit${iri?index?c}(config: Configurator) {
        Unit unit = null;
        unit = new Unit(${q(iri)}, ${optStr(unit.prefixIri)}, ${optStr(unit.scalingOfIri)}, ${optStr(unit.dimensionVectorIri)}, ${optBigDec(unit.conversionMultiplier)}, ${optBigDec(unit.conversionOffset)}, ${optStr(unit.symbol)});
        <#list unit.labels as label>
        unit.addLabel(new LangString(${q(label.string)}, ${optStr(label.languageTag)}));
        </#list>
        <#list unit.quantityKindIris as quantityKindIri>
        unit.addQuantityKind(${q(quantityKindIri)});
        </#list>
        config.units.set(${q(iri)}, unit);
    }
    </#list>


<#list units as iri, unit>
    addUnit${iri?index?c}(config);
</#list>
return units;

}
