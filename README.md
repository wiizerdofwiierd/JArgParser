# JArgParser
 A simple command-line style argument parser for Java

# Using in your project
## Maven [![](https://jitpack.io/v/wiizerdofwiierd/JArgParser.svg)](https://jitpack.io/#wiizerdofwiierd/JArgParser)
Firstly, make sure you have added jitpack to the repositories in your pom.xml:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Then, add JArgParser to your dependencies:
```xml
<dependency>
    <groupId>com.github.wiizerdofwiierd</groupId>
    <artifactId>JArgParser</artifactId>
    <version>VERSION</version>
</dependency>
```
Where `VERSION` is either `latest`, or the tag (Such as `v1.1`)

## Gradle [![](https://jitpack.io/v/wiizerdofwiierd/JArgParser.svg)](https://jitpack.io/#wiizerdofwiierd/JArgParser)
Add the repository to your build.gradle:
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Then add the dependency:
```gradle
dependencies {
    implementation 'com.github.wiizerdofwiierd:JArgParser:Tag'
}
```

## Jar
You can download the latest release [here](https://github.com/wiizerdofwiierd/JArgParser/releases/latest) 

# Code examples
## Basic example
Here is a very basic example. This will look for a `-message` command-line parameter and print the message to `System.out`
```java
import xyz.rybicki.util.jargparser.*;

class BasicExample{
    public static void main(String[] args){
        new ArgParser().withArgument("-message", new Value() {
            @Override
            public void handle(Object value){
                System.out.println("Your message is: " + value);
            }
        }).parse(args);
    }
}
```
If you were to compile this into BasicExample.jar and run it in the command-line using the following:  
`java -jar BasicExample.jar -message Hello`

Your output would be:  
`Your message is: Hello`

If you need to pass a value containing spaces, wrap it in quotes: `-message "Hello world!"`

## Required arguments
Arguments are optional by default, but can be made required:
```java
import xyz.rybicki.util.jargparser.*;

class RequiredArgumentExample{
    public static void main(String[] args){
        new ArgParser().withArgument("-message", new Value() {
            @Override
            public void handle(Object value){
                System.out.println("Your message is: " + value);
            }
        }, true /* <- Setting this parameter to true makes the argument required */).parse(args);
    }
}
```
If this argument is not provided, the application will exit, and you will receive the following output:
```
Error when parsing arguments: One or more required arguments are missing:
Missing: -message
```
## Flags
You can also specify a **Flag**, which instead of having a value, simply executes something when it is present:
```java
import xyz.rybicki.util.jargparser.*;

class FlagExample{
    public static void main(String[] args){
        new ArgParser().withArgument("--hello", new Flag(){
            @Override
            public void handle(Object value){
                System.out.println("The --hello flag is set!");
            }
        }).parse(args);
    }
}
```
Compiling this and running it using `java -jar FlagExample.jar --hello` would result in the output `The --hello flag is set!`

## Argument priorities
Arguments can be given a priority, which determines the order in which they are handled. This is useful when you want to validate arguments in a certain order, such as making sure that a username is given before checking anything else. Or if you have code in that argument's `handle` method that needs to be executed before the other arguments

If no priority is specified, the arguments will be handled in the order they are given  

*Note: The `priority` parameter represents the order that the argument is handled in rather than its actual priority.  
Arguments with **lower** numbers will be executed first. The parameter name will be changed in a future update to reflect this*
```java
import xyz.rybicki.util.jargparser.*;

class ArgumentOrderExample{
    private String username;
    private String password;
	
    public static void main(String[] args){
        new ArgParser().withArgument("-message", new Flag(){
            @Override
            public void handle(Object value){
                System.out.println("Your message is: " + value);
            }
        })
        .withArgument("-password", -1, new Value(){
            @Override
            public void handle(Object value){
                Main.password = value;
            }
        }, true)
        .withArgument("-username", -2, new Value(){
            @Override
            public void handle(Object value){
            	Main.username = value;
            }
        }, true).parse(args);
    }
}
```
This enables the use of three arguments:
- A **required** `-username` argument, which is handled first
- A **required** `-password` argument, which is handled second
- An *optional* `-message` argument, which will be handled last

# Planned features
- A way of adding and retrieving arguments that are simple key/value pairs without requiring an anonymous class implementation
- Custom messages when missing required arguments
