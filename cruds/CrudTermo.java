package cruds;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
                    ArrayList<RelacaoIdNomeTermo> resultadoBuscaTermoId = arquivoRelacaoIdNomesETermos
                            .buscarPeloTermoId(objetoTermo.getId());

                    // obtem o ultimo registro (supostamente com espaço ainda) da lista de
                    // associações de termoId com nomesIds
                    RelacaoIdNomeTermo relacaoIdNomeTermo = resultadoBuscaTermoId.get(0);
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
            objetoTermo = new Termo(removerAcentos(termo.toLowerCase()));
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

    public ArrayList<Integer> obterNomeIdsEmBuscaPorTermos(ArrayList<String> termos) throws Exception {
        ArrayList<Termo> termosExistentes = obterDadosTermosExistentes(termos);
        ArrayList<ArrayList<Integer>> listaNomesIdsPorTermo = new ArrayList<ArrayList<Integer>>();

        for (Termo termo : termosExistentes) {
            ArrayList<RelacaoIdNomeTermo> relacaoIdNomeTermosExistentes = arquivoRelacaoIdNomesETermos
                    .buscarPeloTermoId(termo.getId());
            if (!relacaoIdNomeTermosExistentes.isEmpty()) {
                /*
                 * junta todos os ids de nomes relacionados a um termo específico em um só vetor
                 * devido ao fato de que os registros do arquivo de RelacaoIdNomeTermo armazenam
                 * apenas 10 ids em cada um. Nesse ponto não é relevante saber a qual termo
                 * aquela lista de ids pertence, mas sim que cada vetor de inteiros pertencem a
                 * um termo em espeficio apenas.
                 */

                ArrayList<Integer> ids = new ArrayList<Integer>();
                for (RelacaoIdNomeTermo relacaoIds : relacaoIdNomeTermosExistentes) {
                    for (int id : relacaoIds.getIdsNomesOcorrencias()) {
                        ids.add(id);
                    }
                }
                listaNomesIdsPorTermo.add(ids);
            } else {
                System.out.println("\nUm dos termos inseridos não está presente em nenhum dos nomes cadastrados.");
                return null;
            }
        }

        return obtemIdsComunsATodasAsListas(listaNomesIdsPorTermo);
    }

    /*
     * Método obtemIdsComunsATodasAsListas: verifica em todas as sublistas de
     * listaNomesIdsPorTermo se eles possuem os mesmos nomesIds da primeira; dessa
     * forma são filtrados os ids apenas dos nomes que estão presentes em todas as
     * listas de todos os termos da busca
     */
    private ArrayList<Integer> obtemIdsComunsATodasAsListas(ArrayList<ArrayList<Integer>> listaNomesIdsPorTermo) {
        ArrayList<Integer> resultadoBusca = new ArrayList<Integer>();

        if (!listaNomesIdsPorTermo.isEmpty()) {
            ArrayList<Integer> arrayReferencia = listaNomesIdsPorTermo.get(0);
            for (int i = 0; i < arrayReferencia.size(); i++) {
                boolean contemIdAtualEmTodasAsListas = true;
                for (int j = 1; j < listaNomesIdsPorTermo.size() && contemIdAtualEmTodasAsListas; j++) {
                    if (!listaNomesIdsPorTermo.get(j).contains(arrayReferencia.get(i))) {
                        contemIdAtualEmTodasAsListas = false;
                    }
                }
                if (contemIdAtualEmTodasAsListas) {
                    resultadoBusca.add(arrayReferencia.get(i));
                }
            }
        }
        return resultadoBusca;
    }

    /*
     * Método obterDadosTermosExistentes(ArrayList<String> termos): a partir de uma
     * lista de Strings busca no arquivo de termos se já fora cadastrada alguma
     * palavra com o termo informado. Em caso positivo, armazena os dados do termo
     * em um array e o retorna para o método anterior.
     */
    private ArrayList<Termo> obterDadosTermosExistentes(ArrayList<String> termos) throws Exception {
        ArrayList<Termo> termosExistentes = new ArrayList<Termo>();

        for (String termo : termos) {
            Termo termoEncontrado = (Termo) arquivoTermos.buscar(removerAcentos(termo.toLowerCase()));
            if (termoEncontrado != null) {
                termosExistentes.add(termoEncontrado);
            }
        }

        return termosExistentes;
    }
}
