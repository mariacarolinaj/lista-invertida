package cruds;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import entidades.Nome;
import entidades.RelacaoIdNomeTermo;
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
            if (nomes.length > 0) {

                for (int i = 0; i < nomes.length; i++) {
                    Nome nome = (Nome) nomes[i];
                    System.out.println("\t- " + nome.getNome() + " (id: " + nome.getId() + ")");
                }
            } else {
                System.out.println("Ainda não foi cadastrado nenhum nome.");
            }

        } catch (Exception e) {
            System.out.println("Ocorreu um erro ao acessar a base de dados. Tente novamente.");
        }

        Util.mensagemContinuar();
    }

    public void buscarPorTermos() throws Exception {
        try {
            System.out.println("*.*.*.*.*.* Busca através de termos *.*.*.*.*.*\n");
            System.out.println("Informe os termos que deseja utilizar na busca:");

            String termoInserido;
            System.out.print("> ");
            termoInserido = br.readLine();
            
            String[] termosBusca = termoInserido.split(" ");
            ArrayList<Integer> nomesIds = crudTermo.obterNomeIdsEmBuscaPorTermos(termosBusca);
            System.out.println();

            if (nomesIds != null && !nomesIds.isEmpty()) {
                System.out.println("Foram encontrados os seguintes registros para os termos inseridos na busca:\n");
                for (Integer id : nomesIds) {
                    Nome nome = (Nome) arquivoNomes.buscar(id);
                    System.out.println("\t- " + nome.getNome() + " (id: " + nome.getId() + ")");
                }
            } else {
                System.out.println("Não foi encontrado nenhum registro compatível com os termos inseridos na busca.");
            }
        } catch (Exception e) {
            System.out.println("Um erro inesperado ocorreu. Tente novamente.");
        }
        Util.mensagemContinuar();
    }
}