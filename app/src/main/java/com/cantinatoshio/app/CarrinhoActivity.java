package com.cantinatoshio.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cantinatoshio.app.Database.LoginTabela;
import com.google.android.material.snackbar.Snackbar;
import com.cantinatoshio.app.Database.PedidoHelper;

import java.util.ArrayList;

public class CarrinhoActivity extends AppCompatActivity
{

    RecyclerView lista_teste;
    AdapterCarrinho adapterCarrinho;
    ArrayList<Produto> carrinho;
    Button limpar, btnEnviarPedido;
    TextView totalPedido;
    double calculaTotal = 0;
    String total;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrinho);
        carrinho = new ArrayList<>();
        adapterCarrinho = new AdapterCarrinho(getApplicationContext(), carrinho);
        lista_teste = findViewById(R.id.testeRecView);
        limpar = findViewById(R.id.btnLimpar);
        btnEnviarPedido = findViewById(R.id.btnEviarPedido);
        totalPedido = findViewById(R.id.totalPedido);

        new GetData().start();
        limpar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                new PedidoHelper(CarrinhoActivity.this).limparCarrinho(CarrinhoActivity.this);

                //limpando a tela

                refresh();

                LoginTabela loginTabela = new LoginTabela(getApplicationContext());
                loginTabela.setLogin(3);

                Cursor cursor = loginTabela.getLogin();
                while (cursor.moveToNext())
                {
                    System.out.println(cursor.getString(0));
                }
            }
        });


        //fazendo o looping para mostrar os dados
        Cursor cursor = new PedidoHelper(this).getPedido();
        while (cursor.moveToNext())
        {
            String titulo = cursor.getString(2);

            //calculando total
            calculaTotal = calculaTotal + Double.parseDouble(cursor.getString(4));

            //1 ID, 2 titulo, 3 qtd, 4 preco
            Produto produto = new Produto(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), selecionaImagem(titulo));
            carrinho.add(produto);
        }


        btnEnviarPedido.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(Cliente.isLoggedIn)
                {
                    int idCliente = Cliente.idCliente;
                    Pedido pedido = new Pedido(idCliente, calculaTotal);
                    boolean envio = pedido.enviarPedido();
                    if(envio)
                    {
                        for (int i = 0; i <=carrinho.size()-1; i++)
                        {
                            pedido.enviarProdutos(carrinho.get(i));
                        }
                        new PedidoHelper(CarrinhoActivity.this).limparCarrinho(CarrinhoActivity.this);

                        Intent intent = new Intent(getApplicationContext(), PedidoRealizado.class);
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(CarrinhoActivity.this, "Não foi possível realizar seu pedido...", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Snackbar snackbar = Snackbar.make(view, "Você primeiro precisa fazer login!", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(Color.rgb(255, 165, 0));
                    snackbar.setTextColor(Color.parseColor("#000000"));
                    snackbar.show();
                }
            }
        });


        total = String.valueOf(calculaTotal);
        totalPedido.setText(total);


        lista_teste = findViewById(R.id.testeRecView);
        lista_teste.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        lista_teste.hasFixedSize();
        lista_teste.setAdapter(adapterCarrinho);

        if(carrinho.size()==0)
        {
            setContentView(R.layout.modelo_emptyactivity);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }

    //atualiza a tela
    private void refresh()
    {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    private int selecionaImagem(String nome)
    {
        int img = 0;
        switch (nome)
        {
            case "Água":
                img = R.drawable.agua;
                break;
            case "Refrigerante":
                img = R.drawable.refri;
                break;
            case "Salada":
                img = R.drawable.salada;
                break;
            case "Fatia de bolo":
                img = R.drawable.bolo;
                break;
            case "Brigadeiro":
                img = R.drawable.brigadeiro;
                break;
            case "Mousse":
                img = R.drawable.mousse;
                break;
            case "Suco Natural":
                img = R.drawable.suco;
                break;
            case "Coxinha":
                img = R.drawable.coxinha;
                break;
            case "Pão de Queijo":
                img = R.drawable.fpaodequeijo;
                break;
            case "Misto Quente":
                img = R.drawable.misto;
                break;
            case "Combo batata frita e bacon":
                img = R.drawable.batatabacon;
                break;
            case "Combo Coca e burger":
                img = R.drawable.hamburgercoca;
                break;
            case "Combo café e pão de queijo":
                img = R.drawable.paocafe;
                break;
        }
        return img;
    }


    //adaptador interno
    private class AdapterCarrinho extends RecyclerView.Adapter<AdapterCarrinho.ViewHolder>
    {

        private ArrayList<Produto> carrinho;
        private Context context;

        // public AdapterCarrinho(){}

        public AdapterCarrinho(Context context, ArrayList<Produto> carrinho)
        {
            this.carrinho = carrinho;
            this.context = context;
        }

        @NonNull
        @Override
        public AdapterCarrinho.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view;
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.modelo_carrinho, parent, false);
            return new AdapterCarrinho.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position)
        {
            holder.midProduto.setText(carrinho.get(position).getIdProduto());
            holder.mnomeProduto.setText(carrinho.get(position).getNomeProduto());
            holder.mdescProduto.setText(carrinho.get(position).getDescProduto());
            holder.mprecoProduto.setText(carrinho.get(position).getPrecoProduto());
            holder.mqtdProduto.setText(carrinho.get(position).getQtdeProduto());
            holder.mImageProduto.setImageResource(carrinho.get(position).getImgProduto());
        }

        @Override
        public int getItemCount()
        {
            return carrinho.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder
        {
            TextView mnomeProduto, mdescProduto, mprecoProduto, mqtdProduto, midProduto;
            ImageView mImageProduto, mdeleteProduto;

            public ViewHolder(@NonNull View itemView)
            {
                super(itemView);
                midProduto = itemView.findViewById(R.id.IDproduto_cart);
                mnomeProduto = itemView.findViewById(R.id.nomeProduto);
                mdescProduto = itemView.findViewById(R.id.descProduto);
                mprecoProduto = itemView.findViewById(R.id.precoProduto);
                mqtdProduto = itemView.findViewById(R.id.qtdProduto);
                mImageProduto = itemView.findViewById(R.id.imgProduto);
                mdeleteProduto = itemView.findViewById(R.id.delete_item);

                mdeleteProduto.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        new PedidoHelper(context.getApplicationContext()).removerItem(midProduto.getText().toString());
                        refresh();
                        Toast.makeText(CarrinhoActivity.this, "Item removido!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

}