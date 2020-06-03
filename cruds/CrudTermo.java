package cruds;

import java.text.Normalizer;

import entidades.Nome;
import entidades.RelacaoIdNomeTermo;
import entidades.Termo;
import util.Util;

public class CrudTermo {
    private static Arquivo<Termo> arquivoTermos;
    private static Arquivo<RelacaoIdNomeTermo> arquivoRelacaoIdNomesETermos;

    public CrudTermo() {
        this.inicializarBaseDados();
    }

    /*
     * Método inicializarBaseDados(): tenta abrir os arquivos da base de dados. Caso
     * eles não existam, são criados dentro da pasta "dados".
     */

    private void inicializarBaseDados() {
        try {
            // tenta abrir os arquivos da base de dados caso existam;
            // se não existirem, são criados
            arquivoTermos = new Arquivo<>(Termo.class.getConstructor(), "termos.db");
            arquivoRelacaoIdNomesETermos = new Arquivo<>(RelacaoIdNomeTermo.class.getConstructor(),
                    "relacaoIdNomesETermos.db");
        } catch (Exception e) {
            System.err.println("Não foi possível inicializar a base de dados dos termos.");
            e.printStackTrace();
        }
    }

    public void incluirTermos(Nome nome) {
        // faz letras ficarem minúsculas e remove acentos antes de separar as palavras
        // do nome recebido por parâmetro
        String[] termos = removerAcentos(nome.getNome().toLowerCase()).split(" ");

        try {
            for (String termo : termos) {
                // apenas inclui o registro do novo termo se a String não estiver vazia E não
                // for uma stop word
                if (!termo.isEmpty() && !isStopWord(termo)) {
                    Termo objetoTermo = obterDadosTermo(termo);
                    RelacaoIdNomeTermo relacaoIdNomeTermo = (RelacaoIdNomeTermo) arquivoRelacaoIdNomesETermos
                            .buscarPeloTermoId(objetoTermo.getId());

                    int quantidadeIdsJaArmazenados = relacaoIdNomeTermo.getQtdTermos();
                    if (quantidadeIdsJaArmazenados < 10) {
                        int[] nomesIds = inserirNovoValor(relacaoIdNomeTermo.getIdsNomesOcorrencias(), nome.getId());
                        relacaoIdNomeTermo.setIdsNomesOcorrencias(nomesIds);
                        relacaoIdNomeTermo.setQtdTermos(quantidadeIdsJaArmazenados + 1);
                        // atualiza o arquivo com o ID do novo nome que possui o termo informado e
                        // incrementa a quantidade em +1
                        arquivoRelacaoIdNomesETermos.atualizar(relacaoIdNomeTermo);
                    } else {
                        int[] nomesIds = { nome.getId() };
                        // cria novo objeto para inclusão de IDs de nomes relacionados ao termo
                        // informado anteriormente
                        RelacaoIdNomeTermo novoRegistroRelacaoIdNomeTermo = new RelacaoIdNomeTermo(objetoTermo.getId(),
                                1, nomesIds);
                        // armazena esse novo registro e obtem seu ID
                        int idNovoRegistroRelacaoIdNomeTermo = arquivoRelacaoIdNomesETermos
                                .incluir(novoRegistroRelacaoIdNomeTermo);

                        // modifica a propriedade "proximo" do registro original do termo apontando para
                        // o novo registro, já que o anterior estava com a lotação máxima (10 IDs)
                        relacaoIdNomeTermo.setProximo(idNovoRegistroRelacaoIdNomeTermo);
                        // atualiza o registro original do termo em questão com o novo apontamento
                        arquivoRelacaoIdNomesETermos.atualizar(relacaoIdNomeTermo);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Problema em: CrudTermo");
            Util.mensagemErroCadastro();
        }
    }

    /*
     * Método inserirNovoValor(int[] vetor, int novoValor): insere um novo inteiro
     * no final de um vetor, aumentando automaticamente seu tamanho em +1.
     */
    private int[] inserirNovoValor(int[] vetor, int novoValor) {
        int[] novoVetor = new int[vetor.length + 1];

        for (int i = 0; i < vetor.length; i++) {
            novoVetor[i] = vetor[i];
        }

        novoVetor[novoVetor.length - 1] = novoValor;

        return novoVetor;
    }

    /*
     * Método obterDadosTermo(String termo): recebe uma String de termo e procura
     * por ela na base de dados de termos; caso já exista um registro para o termo
     * informado retorna esse registro, senão o insere e retorna o registro que
     * acabou de ser inserido.
     */
    private Termo obterDadosTermo(String termo) throws Exception {
        Termo objetoTermo = (Termo) arquivoTermos.buscar(termo);

        if (objetoTermo == null) {
            objetoTermo = new Termo(termo);
            int idTermoInserido = arquivoTermos.incluir(objetoTermo);
            objetoTermo.setId(idTermoInserido);

            this.inserirNovaRelacaoIdNomeTermo(objetoTermo); // cria arquivo de associação de nomes e termos
        }

        return objetoTermo;
    }

    private void inserirNovaRelacaoIdNomeTermo(Termo termo) throws Exception {
        RelacaoIdNomeTermo relacaoNomeTermo = new RelacaoIdNomeTermo(termo);
        arquivoRelacaoIdNomesETermos.incluir(relacaoNomeTermo);
    }

    private static String removerAcentos(String string) {
        return Normalizer.normalize(string, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    private boolean isStopWord(String string) {
        String[] stopWords = { "de", "da", "do", "e", "a", "o", "dos", "das", "as", "os" };
        boolean ehStopWord = false;

        for (String sw : stopWords) {
            if (sw.equals(string)) {
                ehStopWord = true;
            }
        }

        return ehStopWord;
    }
}
