package org.qi4j.tutorials.services.step6;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class LibraryService
    implements Library, Activatable
{
    public static interface LibraryConfiguration
        extends ConfigurationComposite
    {
        Property<String> titles();

        Property<String> authors();

        Property<Integer> copies();
    }

    private HashMap<String, ArrayList<Book>> books;

    public LibraryService( @This Configuration<LibraryConfiguration> config,
                         @Structure ValueBuilderFactory factory
    )
    {
        books = new HashMap<String, ArrayList<Book>>();
        String titles = config.configuration().titles().get();
        String authors = config.configuration().authors().get();
        int copies = config.configuration().copies().get();
        StringTokenizer titlesSt = new StringTokenizer( titles, ",", false );
        StringTokenizer authorSt = new StringTokenizer( authors, ",", false );
        while( titlesSt.hasMoreTokens() )
        {
            String title = titlesSt.nextToken();
            String author = authorSt.nextToken();
            createBook( factory, author, title, copies );
        }
    }

    public Book borrowBook( String author, String title )
    {
        String key = constructKey( author, title );
        ArrayList<Book> copies = books.get( key );
        if( copies == null )
        {
            System.out.println( "Book not available." );
            return null; // Indicate that book is not available.
        }
        Book book = copies.remove( 0 );
        if( book == null )
        {
            System.out.println( "Book not available." );
            return null; // Indicate that book is not available.
        }
        System.out.println( "Book borrowed: " + book.title().get() + " by " + book.author().get() );
        return book;
    }

    public void returnBook( Book book )
    {
        System.out.println( "Book returned: " + book.title().get() + " by " + book.author().get() );
        String key = constructKey( book.author().get(), book.title().get() );
        ArrayList<Book> copies = books.get( key );
        if( copies == null )
        {
            throw new IllegalStateException( "Book " + book + " was not borrowed here." );
        }
        copies.add( book );
    }

    public void activate()
        throws Exception
    {
    }

    public void passivate()
        throws Exception
    {
    }

    private void createBook( ValueBuilderFactory factory, String author, String title, int copies )
    {
        ArrayList<Book> bookCopies = new ArrayList<Book>();
        String key = constructKey( author, title );
        books.put( key, bookCopies );

        for( int i = 0; i < copies; i++ )
        {
            ValueBuilder<Book> builder = factory.newValueBuilder( Book.class );
            Book prototype = builder.prototype();
            prototype.author().set( author );
            prototype.title().set( title );

            Book book = builder.newInstance();
            System.out.println( "Book created: " + book );
            bookCopies.add( book );
        }
    }

    private String constructKey( String author, String title )
    {
        return author + "::" + title;
    }
}