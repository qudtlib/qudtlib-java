# QUDTLib Example

Here are the important bits:

## maven
```
<dependency>
	<groupId>io.github.qudtlib</groupId>
	<artifactId>qudtlib</artifactId>
	<version>${project.version}</version> <!-- remember to use the latest version -->
</dependency>
```

## Java
```java
	public static void main(String[] args) {
		System.out.println(
			"Converting 38.5° Celsius into Fahrenheit: "
			+ Qudt.convert(
					new BigDecimal("38.5"),
					Qudt.Units.DEG_C,
					Qudt.Units.DEG_F));
	}
```
there is a bit more code to look at in [src/main/java/org/example/qudlib/QudtlibExample.java](src/main/java/org/example/qudlib/QudtlibExample.java)
