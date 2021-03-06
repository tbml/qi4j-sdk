/*  Copyright 2009 Tonny Kohar.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.envisage.detail;

import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.association.GenericAssociationInfo;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.util.Classes;
import org.qi4j.envisage.model.descriptor.*;
import org.qi4j.envisage.model.util.DescriptorUtilities;
import org.qi4j.envisage.util.TableRow;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.qi4j.functional.Iterables.first;

/**
 * Implementation of Composite State Panel
 */
public class StatePane
    extends DetailPane
{
    protected ResourceBundle bundle = ResourceBundle.getBundle( this.getClass().getName() );

    private JPanel contentPane;
    private JList methodList;
    private JSplitPane splitPane;
    private JTable methodDetailTable;

    private DefaultListModel methodListModel;
    private MethodDetailTableModel methodDetailTableModel;

    public StatePane( DetailModelPane detailModelPane )
    {
        super( detailModelPane );
        this.setLayout( new BorderLayout() );
        this.add( contentPane, BorderLayout.CENTER );

        methodListModel = new DefaultListModel();
        methodList.setModel( methodListModel );
        methodList.setCellRenderer( new MethodListCellRenderer() );
        methodList.setPrototypeCellValue( "12345678901234567890" );
        methodList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

        methodDetailTableModel = new MethodDetailTableModel();
        methodDetailTable.setModel( methodDetailTableModel );

        TableColumnModel columnModel = methodDetailTable.getColumnModel();
        columnModel.getColumn( 0 ).setPreferredWidth( 75 );
        columnModel.getColumn( 1 ).setPreferredWidth( 400 );

        //splitPane.setDividerLocation( .1 );

        methodList.addListSelectionListener( new ListSelectionListener()
        {
            public void valueChanged( ListSelectionEvent evt )
            {
                methodListValueChanged( evt );
            }
        } );
    }

    public void setDescriptor( Object objectDesciptor )
    {
        clear();

        if( objectDesciptor instanceof CompositeDetailDescriptor )
        {
            CompositeDetailDescriptor descriptor = ( (CompositeDetailDescriptor) objectDesciptor );
            List<CompositeMethodDetailDescriptor> list = DescriptorUtilities.findState( descriptor );
            for( CompositeMethodDetailDescriptor methodDescriptor : list )
            {
                methodListModel.addElement( methodDescriptor );
            }

            if( !methodListModel.isEmpty() )
            {
                methodList.setSelectedIndex( 0 );
            }
        }
        else if( objectDesciptor instanceof ObjectDetailDescriptor )
        {
            // Object does not have state
            return;
        }
    }

    protected void clear()
    {
        methodListModel.clear();
        methodDetailTableModel.clear();
    }

    public void methodListValueChanged( ListSelectionEvent evt )
    {
        if( evt.getValueIsAdjusting() )
        {
            return;
        }
        Object obj = methodList.getSelectedValue();
        if( obj == null )
        {
            methodDetailTableModel.clear();
            return;
        }
        methodDetailTableModel.reload( (CompositeMethodDetailDescriptor) obj );
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        contentPane = new JPanel();
        contentPane.setLayout( new BorderLayout( 0, 0 ) );
        splitPane = new JSplitPane();
        contentPane.add( splitPane, BorderLayout.CENTER );
        final JScrollPane scrollPane1 = new JScrollPane();
        splitPane.setLeftComponent( scrollPane1 );
        methodList = new JList();
        scrollPane1.setViewportView( methodList );
        final JScrollPane scrollPane2 = new JScrollPane();
        splitPane.setRightComponent( scrollPane2 );
        methodDetailTable = new JTable();
        scrollPane2.setViewportView( methodDetailTable );
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return contentPane;
    }

    class MethodDetailTableModel
        extends AbstractTableModel
    {
        /**
         * the column names for this model
         */
        //protected String[] columnNames = { bundle.getString( "Name.Column" ), bundle.getString( "Value.Column" ) };
        protected String[] columnNames = { "Name", "Value" };
        protected ArrayList<TableRow> rows;

        public MethodDetailTableModel()
        {
            rows = new ArrayList<TableRow>();
        }

        public void reload( CompositeMethodDetailDescriptor descriptor )
        {
            clear();

            // mixin type
            rows.add( new TableRow( 2, new Object[]{
                "interface", descriptor.descriptor().method().getDeclaringClass().getSimpleName()
            } ) );

            StringBuilder str = new StringBuilder();
            for (Annotation annotation : descriptor.descriptor().method().getAnnotations())
            {
               String ann = annotation.toString();
               ann = "@"+ann.substring( ann.lastIndexOf('.' )+1);
               str.append( ann ).append( " " );
            }
            str.append(Classes.getSimpleGenericName( descriptor.descriptor().method().getGenericReturnType()));

            rows.add( new TableRow( 2, new Object[]{
                "return", str.toString()
            } ) );

            // concern
            boolean first = true;
            for( MethodConcernDetailDescriptor concern : descriptor.concerns().concerns() )
            {
                if( first )
                {
                    rows.add( new TableRow( 2, new Object[]{ "concern", concern.toString() } ) );
                    first = false;
                }
                else
                {
                    rows.add( new TableRow( 2, new Object[]{ "", concern.toString() } ) );
                }
            }

            // sideEffect
            first = false;
            for( MethodSideEffectDetailDescriptor sideEffect : descriptor.sideEffects().sideEffects() )
            {
                if( first )
                {
                    rows.add( new TableRow( 2, new Object[]{ "sideEffect", sideEffect.toString() } ) );
                    first = false;
                }
                else
                {
                    rows.add( new TableRow( 2, new Object[]{ "", sideEffect.toString() } ) );
                }
            }

            fireTableDataChanged();
        }

        public Object getValueAt( int rowIndex, int columnIndex )
        {
            TableRow row = this.rows.get( rowIndex );
            return row.get( columnIndex );
        }

        public void clear()
        {
            rows.clear();
            fireTableDataChanged();
        }

        public int getColumnCount()
        {
            return columnNames.length;
        }

        public String getColumnName( int col )
        {
            return columnNames[ col ];
        }

        public int getRowCount()
        {
            return rows.size();
        }
    }

    class MethodListCellRenderer
        extends DefaultListCellRenderer
    {
        protected Icon publicIcon;
        protected Icon privateIcon;

        public MethodListCellRenderer()
        {
            try
            {
                publicIcon = new ImageIcon( getClass().getResource( bundle.getString( "ICON_Public" ) ) );
                privateIcon = new ImageIcon( getClass().getResource( bundle.getString( "ICON_Private" ) ) );
            }
            catch( Exception ex )
            {
                throw new RuntimeException( ex );
            }
        }

        public Component getListCellRendererComponent( JList list,
                                                       Object value,
                                                       int index,
                                                       boolean isSelected,
                                                       boolean cellHasFocus
        )
        {
            super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
            if( !( value instanceof CompositeMethodDetailDescriptor ) )
            {
                return this;
            }

            Icon icon = null;
            CompositeMethodDetailDescriptor descriptor = (CompositeMethodDetailDescriptor) value;
            Class compositeClass = first( descriptor.composite().descriptor().types() );
            Class mixinMethodClass = descriptor.descriptor().method().getDeclaringClass();
            if( mixinMethodClass.isAssignableFrom( compositeClass ) )
            {
                icon = publicIcon;
            }
            else
            {
                icon = privateIcon;
            }

            if( icon != null )
            {
                setIcon( icon );
            }

            Method method = descriptor.descriptor().method();
            Class<?> methodReturnType = method.getReturnType();

            if( Property.class.isAssignableFrom( methodReturnType ) )
            {
                Type t = GenericPropertyInfo.getPropertyType( method );
                if( t instanceof Class )
                {
                    setText( method.getName() + ":" + ( (Class) t ).getSimpleName() );
                }
            }
            else if( Association.class.isAssignableFrom( methodReturnType ) )
            {
                Type t = GenericAssociationInfo.getAssociationType( method );
                if( t instanceof Class )
                {
                    setText( method.getName() + "->" + ( (Class) t ).getSimpleName() );
                }
            }
            else if( ManyAssociation.class.isAssignableFrom( methodReturnType ) )
            {
                Type t = GenericAssociationInfo.getAssociationType( method );
                if( t instanceof Class )
                {
                    setText( method.getName() + "=>" + ( (Class) t ).getSimpleName() );
                }
            }

            return this;
        }
    }
}
