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
    /** ${constant.label} */
    public static final ${type} ${constant.javaName} = ${valueFactory}("${constant.localName}");

</#list>

}

