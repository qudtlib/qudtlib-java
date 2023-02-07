<#if copyright??>
    ${copyright}
</#if>
package ${package};

import static io.github.qudtlib.Qudt.${valueFactory};

import io.github.qudtlib.model.${type};

/**
* Constants for QUDT ${type}s.
*/
public abstract class ${typePlural} {

<#list constants as constant>
    /**
     * QUDT ${constant.typeName} constant <a href="${constant.iri}">${constant.iriLocalname}</a>: ${constant.label}<#if constant.symbol.isPresent()> (${constant.symbol.get()})</#if>
    **/
    public static final ${type} ${constant.codeConstantName} = ${valueFactory}("${constant.iriLocalname}");

</#list>

}

