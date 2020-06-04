/*
* Lista Invertida
* Trabalho prático para a disciplina de Algoritmos e Estruturas de Dados III do curso de Ciência da Computação
* Implementado por Maria Carolina Resende Jaudacy em 1/2020 • Matrícula 667477
* PUC Minas - Professor Marcos Kutova
*/

import java.io.*;

import cruds.CrudNome;
import util.Util;

public class ListaInvertida {
    private static InputStream is = System.in;
    private static InputStreamReader isr = new InputStreamReader(is);
    private static BufferedReader br = new BufferedReader(isr);

    private static CrudNome crudNome;

    public static void main(String[] args) throws Exception {
        crudNome = new CrudNome();
        exibirMenu();
    }

    private static void exibirMenu() throws Exception {
        int opcao;
        do {
            System.out.println("*.*.*.*.*.* Lista Invertida *.*.*.*.*.*\n");
            System.out.println("1: Cadastrar novo nome");
            System.out.println("2: Listar nomes cadastrados");
            System.out.println("3: Buscar através de termos");
            System.out.println("0: Sair");

            System.out.print("\nIr para: ");
            opcao = Integer.parseInt(br.readLine());

            Util.limparTela();

            switch (opcao) {
                case 0: // não é preciso fazer nada; o programa se encerrará
                    break;
                case 1:
                    crudNome.incluirNovoNome();
                    break;
                case 2:
                    crudNome.listarNomes();
                    break;
                case 3:
                    crudNome.buscarPorTermos();
                    break;
                default:
                    Util.mensagemTenteNovamente();
                    Util.mensagemContinuar();
            }
        } while (opcao != 0);

    }
}