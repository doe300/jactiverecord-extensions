JActiveRecord-Extensions
=============
A set of extensions for [doe300/jactiverecord](https://github.com/doe300/jactiverecord).

This extensions are in a separate package, because most of them require extra libraries are too specific to include
into the main repository.

Here is a list of the extensions so far:

- **JavaBeanRecord**: an active-record which supports *PropertyChangeListeners* and firing change-events.
- **AttributeProperty**: mapping of record-attributes to [*JavaFX Properties*](http://download.java.net/jdk8/jfxdocs/javafx/beans/property/Property.html)
including a generator to automatically generate *properties* from *@AddAttribute*-annotations