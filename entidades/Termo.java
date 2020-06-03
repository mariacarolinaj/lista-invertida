package entidades;

import java.io.*;

// armazena cada termo presente na lista de nomes
public class Termo implements Registro {
    private int id;
    private String termo;

    public Termo(int id, String termo) {
        this.id = id;
        this.termo = termo;
    }

    public void setTermo(String termo) {
        this.termo = termo;
    }

    public String getTermo() {
        return this.termo;
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
        return this.termo;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        final ByteArrayOutputStream dados = new ByteArrayOutputStream();
        final DataOutputStream saida = new DataOutputStream(dados);
        saida.writeInt(this.id);
        saida.writeUTF(this.termo);
        return dados.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        final ByteArrayInputStream dados = new ByteArrayInputStream(ba);
        final DataInputStream entrada = new DataInputStream(dados);
        this.id = entrada.readInt();
        this.termo = entrada.readUTF();
    }
}