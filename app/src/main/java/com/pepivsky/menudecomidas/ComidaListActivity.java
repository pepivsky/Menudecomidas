package com.pepivsky.menudecomidas;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pepivsky.menudecomidas.dummy.DummyContent;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * An activity representing a list of Comidas. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ComidaDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ComidaListActivity extends AppCompatActivity {

    //Constante
    private static final String PATH_FOOD = "comida";

    @BindView(R.id.edtNombre)
    EditText edtNombre;
    @BindView(R.id.edtPrecio)
    EditText edtPrecio;
    @BindView(R.id.btnGuardar)
    Button btnGuardar;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comida_list);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (findViewById(R.id.comida_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        View recyclerView = findViewById(R.id.comida_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
    }


    //Leer datos de Firebase
    private void setupRecyclerView(@NonNull final RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, DummyContent.ITEMS, mTwoPane));
        //Instancia de firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //Referencia a la cual nos conectaremos
        DatabaseReference reference = database.getReference(PATH_FOOD);

        //Listener para gestionar la lista de objetos de nuestra referencia "comida"
        reference.addChildEventListener(new ChildEventListener() {
            //Método al agregar
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                DummyContent.Comida comida = dataSnapshot.getValue(DummyContent.Comida.class);
                //Asignar el valor "clave" que es generada automaticamente al id de nuestro objeto comida
                comida.setId(dataSnapshot.getKey());

                //añadir el elemento al arreglo para generar la lista
                if (!DummyContent.ITEMS.contains(comida)) { //Si no contiene el objeto comida entonces lo agrega
                    DummyContent.addItem(comida);
                }
                //Refrescar el adaptador
                recyclerView.getAdapter().notifyDataSetChanged();
            }

            //Método que se ejecuta al detectar cambios en los elementos de la lista
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                DummyContent.Comida comida = dataSnapshot.getValue(DummyContent.Comida.class);
                //Asignar el valor "clave" que es generada automaticamente al id de nuestro objeto comida
                comida.setId(dataSnapshot.getKey());

                //añadir el elemento al arreglo para generar la lista
                if (DummyContent.ITEMS.contains(comida)) { //Si existe actualizalo
                    DummyContent.ActualizarComida(comida);
                }
                //Refrescar el adaptador
                recyclerView.getAdapter().notifyDataSetChanged();
            }

            //Método que se ejecuta al borrar un elemento de la lista
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                DummyContent.Comida comida = dataSnapshot.getValue(DummyContent.Comida.class);
                //Asignar el valor "clave" que es generada automaticamente al id de nuestro objeto comida
                comida.setId(dataSnapshot.getKey());

                //añadir el elemento al arreglo para generar la lista
                if (DummyContent.ITEMS.contains(comida)) { //Si existe actualizalo
                    DummyContent.EliminarComida(comida);
                }
                //Refrescar el adaptador
                recyclerView.getAdapter().notifyDataSetChanged();
            }

            //Método que se ejcuta cuando un elemento cambia su posición en la lista
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Toast.makeText(ComidaListActivity.this, "Movido", Toast.LENGTH_SHORT).show();
            }

            //Método que se ejecuta cuando se cancela una acción
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ComidaListActivity.this, "Cancelado", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //Método del botón guardar  Insertar objetos en firebase
    @OnClick(R.id.btnGuardar)
    public void onViewClicked() {
        DummyContent.Comida comida = new DummyContent.Comida(edtNombre.getText().toString().trim(),
                edtPrecio.getText().toString().trim());

        //instancia de Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //Referencia
        DatabaseReference reference = database.getReference(PATH_FOOD);

        //push para crear un nuevo nodo en la db el cual será irrepetible
        reference.push().setValue(comida);

        //Limpiar editText
        edtNombre.setText("");
        edtPrecio.setText("");

    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final ComidaListActivity mParentActivity;
        private final List<DummyContent.Comida> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DummyContent.Comida item = (DummyContent.Comida) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(ComidaDetailFragment.ARG_ITEM_ID, item.getId());
                    ComidaDetailFragment fragment = new ComidaDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.comida_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ComidaDetailActivity.class);
                    intent.putExtra(ComidaDetailFragment.ARG_ITEM_ID, item.getId());

                    context.startActivity(intent);
                }
            }
        };


        SimpleItemRecyclerViewAdapter(ComidaListActivity parent,
                                      List<DummyContent.Comida> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.comida_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            //holder.mIdView.setText(mValues.get(position).getId());
            //Mostrar el precio
            holder.mIdView.setText("$" + mValues.get(position).getPrecio());
            holder.mContentView.setText(mValues.get(position).getNombre());


            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
            //Listener para borrar
            holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Instancia de Firebase
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    //Referencia de Firebase
                    DatabaseReference reference = database.getReference(PATH_FOOD);
                    //Obtener el objeto seleccionado de la lista mdeidante el id y eliminarlo con el método "removeValue"
                    reference.child(mValues.get(position).getId()).removeValue();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;
            //Botón Borrar
            @BindView(R.id.btnDelete)
            Button btnDelete;

            ViewHolder(View view) {
                super(view);
                //Para el botón de borrar
                ButterKnife.bind(this,view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.nombre);
            }
        }
    }
}
