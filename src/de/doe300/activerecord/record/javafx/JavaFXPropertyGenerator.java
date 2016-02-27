/*
 * The MIT License
 *
 * Copyright 2015 doe300.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.doe300.activerecord.record.javafx;

import de.doe300.activerecord.annotations.ProcessorUtils;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.attributes.generation.AddAttribute;
import de.doe300.activerecord.record.attributes.generation.AddAttributes;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javax.annotation.Nonnull;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Annotation-processor to generate JavaFX properties for the record's attributes.
 *
 * @author doe300
 * @see Property
 */
@SupportedSourceVersion( SourceVersion.RELEASE_8 )
@SupportedAnnotationTypes( 
{
	"de.doe300.activerecord.record.attributes.generation.AddAttribute",
	"de.doe300.activerecord.record.attributes.generation.AddAttributes",
} )
public class JavaFXPropertyGenerator extends AbstractProcessor
{

	private final Set<String> processedElements = new HashSet<>( 10 );

	@Override
	public boolean process( Set<? extends TypeElement> annotations, RoundEnvironment roundEnv )
	{
		roundEnv.getElementsAnnotatedWith( AddAttribute.class ).forEach( (final Element e) ->
		{
			final AddAttribute[] addAttributes = e.getAnnotationsByType( AddAttribute.class );
			final TypeElement recordTypeElement = ( TypeElement ) e;
			processAddAttributes( recordTypeElement, addAttributes );
		} );

		roundEnv.getElementsAnnotatedWith( AddAttributes.class ).forEach( (final Element e) ->
		{
			final AddAttribute[] addAttributes = e.getAnnotation( AddAttributes.class ).value();
			final TypeElement recordTypeElement = ( TypeElement ) e;
			processAddAttributes( recordTypeElement, addAttributes );
		} );
		//we do not claim the annotations, another processor may use them
		return false;
	}

	private void processAddAttributes( final TypeElement recordTypeElement, final AddAttribute[] addAttributes )
	{
		if ( processedElements.contains( recordTypeElement.getQualifiedName().toString() ) )
		{
			return;
		}
		processedElements.add( recordTypeElement.getQualifiedName().toString() );
		processingEnv.getMessager().printMessage( Diagnostic.Kind.NOTE, "Creating dummy stuff");

		final List<String> usedAttributeNames = ProcessorUtils.getAllAttributeNames( processingEnv, recordTypeElement );

		//we can't write into an existing source-file so we must create a new one
		try
		{
			final String generatedFileName = recordTypeElement.getSimpleName() + "Properties";
			JavaFileObject destFile = processingEnv.getFiler().createSourceFile( recordTypeElement.getQualifiedName()
					+ "Properties", recordTypeElement );
			try (Writer writer = destFile.openWriter())
			{
				//we create a class with given attribute-methods as default-methods and extending ActiveRecord
				writer.append( "package " );
				writer.append( processingEnv.getElementUtils().getPackageOf( recordTypeElement ).getQualifiedName().
						toString() );
				writer.append( ";\n" );

				writer.append( "import " ).append( ReadOnlyProperty.class.getCanonicalName() ).append( ";\n" );
				writer.append( "import " ).append( Property.class.getCanonicalName() ).append( ";\n" );
				writer.append( "import " ).append( AttributeProperty.class.getCanonicalName() ).append( ";\n" );

				//TODO write @Generated annotation (somehow netbeans can't find it)
				//we can't create a new property for every method-call, so we need local variables with the properties and add bindings
				//for that we need to create a class, not an interface
				writer.append( "abstract class " ).append( generatedFileName ).append( " implements " ).
						append( ActiveRecord.class.getCanonicalName() );

				writer.append( " {\n\n" );

				//write attribute-methods to file
				for ( final AddAttribute addAttribute : addAttributes )
				{
					//check if methods for attribute already exist
					if ( usedAttributeNames.contains( addAttribute.name() ) )
					{
						processingEnv.getMessager().printMessage( Diagnostic.Kind.NOTE, "Attribute-name '"
								+ addAttribute.name() + "' already in use, skipping", recordTypeElement );
						continue;
					}
					final TypeElement classElement = ( TypeElement ) ProcessorUtils.getTypeMirror( processingEnv,
							addAttribute::type ).asElement();

					final boolean writeableProperty = addAttribute.hasSetter();

					writer.append( generateProperty( classElement.getQualifiedName().toString(), addAttribute.name(), writeableProperty ) );
				}

				writer.append( "}" );
			}
			processingEnv.getMessager().printMessage( Diagnostic.Kind.NOTE, "Generated: " + processingEnv.
					getElementUtils().getPackageOf( recordTypeElement ).getQualifiedName().toString() + '.'
					+ generatedFileName,
					recordTypeElement );

			//warn if type does not extend generated type
			if ( !recordTypeElement.getSuperclass().toString().equals( generatedFileName ) )
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Type '" + recordTypeElement.
						getSimpleName()
						+ "' does not extend the generated type: " + generatedFileName, recordTypeElement );
			}
		}
		catch ( IOException ex )
		{
			processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, ex.getMessage(), recordTypeElement );
		}
	}

	@Nonnull
	private static String generateProperty( @Nonnull final String type, @Nonnull final String attributeName, final boolean writeable )
	{
		StringBuilder sb = new StringBuilder(1000);
		//private final AttributeProperty<type> attributeNameProperty = new AttributeProperty<type>(this, "attributeName", type.class);
		sb.append( "\tprivate final AttributeProperty<").append( type).append( "> ").
				append( attributeName).append( "Property = new AttributeProperty<>(this, \"").append( attributeName ).
				append( "\", ").append( type).append( ".class);\n\n");
		
		
		//public PropertyType<type> attributeNameProperty() {
		sb.append( "\tpublic ").append( writeable ? "Property<" : "ReadOnlyProperty<").append( type).
				append( "> ").append( attributeName).append( "Property() {\n");
		//return attributeNameProperty;
		sb.append( "\t\treturn ").append( attributeName).append( "Property;\n");
		//}
		sb.append( "\t}\n\n");
		
		//public type getAttributeName() {
		sb.append( "\tpublic ").append( type).append(" get").append( Character.toUpperCase( attributeName.charAt( 0))).
				append( attributeName.substring( 1)).append( "() {\n");
		//return attributeNameProperty.get();
		sb.append( "\t\treturn ").append( attributeName).append( "Property.get();\n");
		//}
		sb.append( "\t}\n\n");
		
		if(writeable)
		{
			//public void setAttributeName(type value){
			sb.append( "\tpublic void set").append( Character.toUpperCase( attributeName.charAt( 0))).
				append( attributeName.substring( 1)).append( "(final " ).append( type ).append( " value) {\n");
			//attributeNameProperty.set(value);
			sb.append( "\t\t").append( attributeName).append( "Property.set(value);\n");
			//}
			sb.append( "\t}\n\n");
		}
		
		return sb.toString();
	}
}
