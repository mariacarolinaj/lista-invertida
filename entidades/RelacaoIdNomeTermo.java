package entidades;

import java.io.*;

public class RelacaoIdNomeTermo implements Registro {
    private int id; // id do registro
    private int idTermo; // id do termo presente no arquivo de termos
    private int qtdTermos; // "qtdTermos" serve para controle. O máximo deve ser 10 termos no mesmo
    // registro; caso apareça mais que 10 registros, a propriedade "proximo" indica
    // o id do registro que continua o armazenamento das ocorrências.
    private int[] idsNomesOcorrencias;
    private int proximo; // caso existam mais que 10 registros, esta propriedade indica o id do proximo
    // registro que possui a continuação da listagem dos ids de nomes que possuem o
    // termo indicado; -1 indica que NÃO existe uma continuação do arquivo.

    public RelacaoIdNomeTermo() {
    }

    public RelacaoIdNomeTermo(int id, int idTermo, int qtdTermos, int[] idsNomesOcorrencias) {
        this.setId(id);
        this.setIdTermo(idTermo);
        this.setQtdTermos(qtdTermos);
        this.setIdsNomesOcorrencias(idsNomesOcorrencias);
        this.setProximo(-1);
    }

    public RelacaoIdNomeTermo(int idTermo, int qtdTermos, int[] idsNomesOcorrencias) {
        this.setIdTermo(idTermo);
        this.setQtdTermos(qtdTermos);
        this.setIdsNomesOcorrencias(idsNomesOcorrencias);
        this.setProximo(-1);
    }

    public int getIdTermo() {
        return idTermo;
    }

    public void setIdTermo(int idTermo) {
        this.idTermo = idTermo;
    }

    public int getQtdTermos() {
        return qtdTermos;
    }

    public void setQtdTermos(int qtdTermos) {
        this.qtdTermos = qtdTermos;
    }

    public int[] getIdsNomesOcorrencias() {
        return idsNomesOcorrencias;
    }

    public void setIdsNomesOcorrencias(int[] idsNomesOcorrencias) {
        this.idsNomesOcorrencias = idsNomesOcorrencias;
    }

    public int getProximo() {
        return proximo;
    }

    public void setProximo(int proximo) {
        this.proximo = proximo;
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
        return this.id + "->" + this.idTermo;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        final ByteArrayOutputStream dados = new ByteArrayOutputStream();
        final DataOutputStream saida = new DataOutputStream(dados);
        saida.writeInt(this.id);
        saida.writeInt(this.idTermo);
        saida.writeInt(this.qtdTermos);
        saida.writeUTF(this.concatenarIds());
        saida.writeInt(this.proximo);
        return dados.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        final ByteArrayInputStream dados = new ByteArrayInputStream(ba);
        final DataInputStream entrada = new DataInputStream(dados);
        this.id = entrada.readInt();
        this.idTermo = entrada.readInt();
        this.qtdTermos = entrada.readInt();
        this.idsNomesOcorrencias = this.segregarIds(entrada.readUTF());
        this.proximo = entrada.readInt();
    }

    // concatenarIds(): método para concatenar a lista de ids de ocorrências em uma
    // String de modo a armazená-los no arquivo correspondente.
    private String concatenarIds() {
        String ids = "";

        if (this.qtdTermos > 0) {
            for (int i = 0; i < this.qtdTermos; i++) {
                ids += this.idsNomesOcorrencias[i] + ",";
            }
            // remove a última vírgula e retorna a String com os ids concatenados
            ids = ids.substring(0, ids.length() - 2);
        }

        return ids;
    }

    // segregarIds(String ids): método para segregar os ids retornados do arquivo
    // correspondente, que é retornado em forma de String separados por vírgula
    private int[] segregarIds(String ids) {
        String[] idsSegregadosString = ids.split(",");
        int[] idsSegregadosInt = new int[idsSegregadosString.length];

        for (int i = 0; i < idsSegregadosString.length; i++) {
            idsSegregadosInt[i] = Integer.parseInt(idsSegregadosString[i]);
        }

        return idsSegregadosInt;
    }
}