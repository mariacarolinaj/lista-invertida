package entidades;

import java.io.*;

// armazena o nome completo de cada item do texto (ou seja, vários termos compoem um registro de nome)
public class Nome implements Registro {
    private int id;
    private String nome;

    public Nome(int id, String nome) {
        this.setId(id);
        this.setNome(nome);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getSecudaryKey() {
        return this.nome;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        final ByteArrayOutputStream dados = new ByteArrayOutputStream();
        final DataOutputStream saida = new DataOutputStream(dados);
        saida.writeInt(this.id);
        saida.writeUTF(this.nome);
        return dados.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        final ByteArrayInputStream dados = new ByteArrayInputStream(ba);
        final DataInputStream entrada = new DataInputStream(dados);
        this.id = entrada.readInt();
        this.nome = entrada.readUTF();
    }
}