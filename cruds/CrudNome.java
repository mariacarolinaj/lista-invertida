package cruds;

import java.io.*;

import entidades.Nome;
import util.Util;

public class CrudNome {

    private static InputStream is = System.in;
    private static InputStreamReader isr = new InputStreamReader(is);
    private static BufferedReader br = new BufferedReader(isr);

    private static Arquivo<Nome> arquivoNomes;

    private static CrudTermo crudTermo;

    public CrudNome() {
        crudTermo = new CrudTermo();
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
            arquivoNomes = new Arquivo<>(Nome.class.getConstructor(), "nomes.db");
        } catch (Exception e) {
            System.err.println("Não foi possível inicializar a base de dados dos nomes.");
            e.printStackTrace();
        }
    }

    public void incluirNovoNome() throws Exception {
        System.out.println("*.*.*.*.*.* Inclusão de novo nome *.*.*.*.*.*\n");
        System.out.print("Informe o nome: ");

        String nome = br.readLine();

        if (nome.isEmpty()) {
            System.out.println("\nO valor informado é inválido. Tente novamente.");
        } else {
            System.out.print("\nConfirmar inclusão de \"" + nome + "\"? (S/N): ");
            char confirmacao = br.readLine().toUpperCase().charAt(0);

            if (confirmacao == 'S') {
                try {
                    Nome objetoNome = new Nome(nome);

                    int idInserido = arquivoNomes.incluir(objetoNome);
                    objetoNome.setId(idInserido);

                    crudTermo.incluirTermos(objetoNome);

                    Util.mensagemSucessoCadastro();
                } catch (Exception e) {
                    Util.mensagemErroCadastro();
                }
            }
        }

        Util.mensagemContinuar();
    }

    public void listarNomes() throws IOException {
        try {
            Object[] nomes = arquivoNomes.listar();
            System.out.println("*.*.*.*.*.* Listagem de Nomes *.*.*.*.*.*\n");
            for (int i = 0; i < nomes.length; i++) {
                Nome nome = (Nome) nomes[i];
                System.out.println((i + 1) + ". " + nome.getNome() + " - id: " + nome.getId());
            }
        } catch (Exception e) {
            System.out.println("Ocorreu um erro ao acessar a base de dados. Tente novamente.");
        }

        Util.mensagemContinuar();
    }
}