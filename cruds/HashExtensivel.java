package cruds;

import java.io.*;

public class HashExtensivel {
   
    String           nomeArquivoDiretorio;
    String           nomeArquivoCestos;
    RandomAccessFile arqDiretorio;
    RandomAccessFile arqCestos;
    int              quantidadeDadosPorCesto;
    Diretorio        diretorio;
    
    class Cesto {

        byte   profundidadeLocal;   // profundidade local do cesto
        short  quantidade;          // quantidade de pares presentes no cesto
        short  quantidadeMaxima;    // quantidade maxima de pares que o cesto pode conter
        int[]  chaves;              // sequência de chaves armazenadas no cesto
        long[] enderecos;           // sequência de enderecos correspondentes às chaves
        short  bytesPorPar;         // tamanho fixo de cada par de chave/endereco em bytes
        short  bytesPorCesto;       // tamanho fixo do cesto em bytes

        public Cesto(int qtdmax) throws Exception {
            this(qtdmax, 0);
        }

        public Cesto(int qtdmax, int pl) throws Exception {
            if(qtdmax>32767)
                throw new Exception("Quantidade maxima de 32.767 elementos");
            if(pl>127)
                throw new Exception("Profundidade local maxima de 127 bits");
            profundidadeLocal = (byte)pl;
            quantidade = 0;
            quantidadeMaxima = (short)qtdmax;
            chaves = new int[quantidadeMaxima];
            enderecos = new long[quantidadeMaxima];
            bytesPorPar = 12;  // int + long
            bytesPorCesto = (short)(bytesPorPar * quantidadeMaxima + 3);
        }

        public byte[] toByteArray() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(profundidadeLocal);
            dos.writeShort(quantidade);
            int i=0;
            while(i<quantidade) {
                dos.writeInt(chaves[i]);
                dos.writeLong(enderecos[i]);
                i++;
            }
            while(i<quantidadeMaxima) {
                dos.writeInt(0);
                dos.writeLong(0);
                i++;
            }
            return baos.toByteArray();            
        }

        public void fromByteArray(byte[] ba) throws IOException {
            ByteArrayInputStream bais = new ByteArrayInputStream(ba);
            DataInputStream dis = new DataInputStream(bais);
            profundidadeLocal = dis.readByte();
            quantidade = dis.readShort();
            int i=0;
            while(i<quantidadeMaxima) {
                chaves[i] = dis.readInt();
                enderecos[i] = dis.readLong();
                i++;
            }
        }

        public boolean insere(int c, long e) {
            if(cestoCheio())
                return false;
            int i=quantidade-1;
            while(i>=0 && c<chaves[i]) {
                chaves[i+1] = chaves[i];
                enderecos[i+1] = enderecos[i];
                i--;
            }
            i++;
            chaves[i] = c;
            enderecos[i] = e;
            quantidade++;
            return true;
        }

        public long busca(int c) {
            if(cestoVazio())
                return -1;
            int i=0;
            while(i<quantidade && c>chaves[i])
                i++;
            if(i<quantidade && c==chaves[i])
                return enderecos[i];
            else
                return -1;        
        }

        public boolean remove(int c) {
            if(cestoVazio())
                return false;
            int i=0;
            while(i<quantidade && c>chaves[i])
                i++;
            if(c==chaves[i]) {
                while(i<quantidade-1) {
                    chaves[i] = chaves[i+1];
                    enderecos[i] = enderecos[i+1];
                    i++;
                }
                quantidade--;
                return true;
            }
            else
                return false;        
        }

        public boolean cestoVazio() {
            return quantidade == 0;
        }

        public boolean cestoCheio() {
            return quantidade == quantidadeMaxima;
        }

        public String toString() {
            String s = "\nProfundidade Local: "+profundidadeLocal+
                       "\nQuantidade: "+quantidade+
                       "\n| ";
            int i=0;
            while(i<quantidade) {
                s += chaves[i] + ";" + enderecos[i] + " | ";
                i++;
            }
            while(i<quantidadeMaxima) {
                s += "-;- | ";
                i++;
            }
            return s;
        }

        public int tamanho() {
            return bytesPorCesto;
        }

    }

    class Diretorio {

        byte   profundidadeGlobal;
        long[] enderecos;

        public Diretorio() {
            profundidadeGlobal = 0;
            enderecos = new long[1];
            enderecos[0] = 0;
        }

        public boolean atualizaEndereco(int p, long e) {
            if(p>Math.pow(2,profundidadeGlobal))
                return false;
            enderecos[p] = e;
            return true;
        }

        public byte[] toByteArray() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(profundidadeGlobal);
            int quantidade = (int)Math.pow(2,profundidadeGlobal);
            int i=0;
            while(i<quantidade) {
                dos.writeLong(enderecos[i]);
                i++;
            }
            return baos.toByteArray();            
        }

        public void fromByteArray(byte[] ba) throws IOException {
            ByteArrayInputStream bais = new ByteArrayInputStream(ba);
            DataInputStream dis = new DataInputStream(bais);
            profundidadeGlobal = dis.readByte();
            int quantidade = (int)Math.pow(2,profundidadeGlobal);
            enderecos = new long[quantidade];
            int i=0;
            while(i<quantidade) {
                enderecos[i] = dis.readLong();
                i++;
            }
        }

        public String toString() {
            String s = "\nProfundidade global: "+profundidadeGlobal;
            int i=0;
            int quantidade = (int)Math.pow(2,profundidadeGlobal);
            while(i<quantidade) {
                s += "\n" + i + ": " + enderecos[i];
                i++;
            }
            return s;
        }

        protected long endereco(int p) {
            if(p>Math.pow(2,profundidadeGlobal))
                return -1;
            return enderecos[p];
        }

        protected boolean duplica() {
            if(profundidadeGlobal==127)
                return false;
            profundidadeGlobal++;
            int q1 = (int)Math.pow(2,profundidadeGlobal-1);
            int q2 = (int)Math.pow(2,profundidadeGlobal);
            long[] novosEnderecos = new long[q2];
            int i=0;
            while(i<q1) {
                novosEnderecos[i]=enderecos[i];
                i++;
            }
            while(i<q2) {
                novosEnderecos[i]=enderecos[i-q1];
                i++;
            }
            enderecos = novosEnderecos;
            return true;
        }

        protected int hash(int chave) {
            return chave % (int)Math.pow(2, profundidadeGlobal);
        }
        
        protected int hash2(int chave, int pl) { // calculo do hash para uma dada profundidade local
            return chave % (int)Math.pow(2, pl);            
        }

    }
    
    
    
    public HashExtensivel(int n, String nd, String nc ) throws Exception {
        quantidadeDadosPorCesto = n;
        nomeArquivoDiretorio = nd;
        nomeArquivoCestos = nc;
        
        File d = new File("dados");
        if( !d.exists() )
            d.mkdir();
        
        arqDiretorio = new RandomAccessFile("dados/"+nomeArquivoDiretorio,"rw");
        arqCestos = new RandomAccessFile("dados/"+nomeArquivoCestos,"rw");

        // Se o diretorio ou os cestos estiverem vazios, cria um novo diretorio e lista de cestos
        if(arqDiretorio.length()==0 || arqCestos.length()==0) {

            // Cria um novo diretorio, com profundidade de 0 bits (1 unico elemento)
            diretorio = new Diretorio();
            byte[] bd = diretorio.toByteArray();
            arqDiretorio.write(bd);
            
            // Cria um cesto vazio, ja apontado pelo unico elemento do diretorio
            Cesto c = new Cesto(quantidadeDadosPorCesto);
            bd = c.toByteArray();
            arqCestos.seek(0);
            arqCestos.write(bd);
        }
    }
    
    public boolean insere(int chave, long endereco) throws Exception {
        
        //Carrega o diretorio
        byte[] bd = new byte[(int)arqDiretorio.length()];
        arqDiretorio.seek(0);
        arqDiretorio.read(bd);
        diretorio = new Diretorio();
        diretorio.fromByteArray(bd);        
        
        // Identifica a hash do diretorio,
        int i = diretorio.hash(chave);
        
        // Recupera o cesto
        long enderecoCesto = diretorio.endereco(i);
        Cesto c = new Cesto(quantidadeDadosPorCesto);
        byte[] ba = new byte[c.tamanho()];
        arqCestos.seek(enderecoCesto);
        arqCestos.read(ba);
        c.fromByteArray(ba);
        
        // Testa se a chave ja não existe no cesto
        if(c.busca(chave)!=-1)
            throw new Exception("Chave ja existe");     

        // Testa se o cesto ja não esta cheio
        // Se não estiver, insere o par de chave e endereco
        if(!c.cestoCheio()) {
            // Insere a chave no cesto e o atualiza 
            c.insere(chave, endereco);
            arqCestos.seek(enderecoCesto);
            arqCestos.write(c.toByteArray());
            return true;        
        }
        
        // Duplica o diretorio
        byte pl = c.profundidadeLocal;
        if(pl>=diretorio.profundidadeGlobal)
            diretorio.duplica();
        byte pg = diretorio.profundidadeGlobal;

        // Cria os novos cestos, com os seus enderecos no arquivo de cestos
        Cesto c1 = new Cesto(quantidadeDadosPorCesto, pl+1);
        arqCestos.seek(enderecoCesto);
        arqCestos.write(c1.toByteArray());

        Cesto c2 = new Cesto(quantidadeDadosPorCesto, pl+1);
        long novoEndereco = arqCestos.length();
        arqCestos.seek(novoEndereco);
        arqCestos.write(c2.toByteArray());
        
        // Atualiza os enderecos no diretorio
        int inicio = diretorio.hash2(chave, c.profundidadeLocal);
        int deslocamento = (int)Math.pow(2,pl);
        int max = (int)Math.pow(2,pg);
        boolean troca = false;
        for(int j=inicio; j<max; j+=deslocamento) {
            if(troca)
                diretorio.atualizaEndereco(j,novoEndereco);
            troca=!troca;
        }
        
        // Atualiza o arquivo do diretorio
        bd = diretorio.toByteArray();
        arqDiretorio.seek(0);
        arqDiretorio.write(bd);
        
        // Reinsere as chaves
        for(int j=0; j<c.quantidade; j++) {
            insere(c.chaves[j], c.enderecos[j]);
        }
        insere(chave,endereco);
        return false;   

    }
    
    public long busca(int chave) throws Exception {
        
        //Carrega o diretorio
        byte[] bd = new byte[(int)arqDiretorio.length()];
        arqDiretorio.seek(0);
        arqDiretorio.read(bd);
        diretorio = new Diretorio();
        diretorio.fromByteArray(bd);        
        
        // Identifica a hash do diretorio,
        int i = diretorio.hash(chave);
        
        // Recupera o cesto
        long enderecoCesto = diretorio.endereco(i);
        Cesto c = new Cesto(quantidadeDadosPorCesto);
        byte[] ba = new byte[c.tamanho()];
        arqCestos.seek(enderecoCesto);
        arqCestos.read(ba);
        c.fromByteArray(ba);
        
        return c.busca(chave);
    }
    
    public boolean remove(int chave) throws Exception {
        
        //Carrega o diretorio
        byte[] bd = new byte[(int)arqDiretorio.length()];
        arqDiretorio.seek(0);
        arqDiretorio.read(bd);
        diretorio = new Diretorio();
        diretorio.fromByteArray(bd);        
        
        // Identifica a hash do diretorio,
        int i = diretorio.hash(chave);
        
        // Recupera o cesto
        long enderecoCesto = diretorio.endereco(i);
        Cesto c = new Cesto(quantidadeDadosPorCesto);
        byte[] ba = new byte[c.tamanho()];
        arqCestos.seek(enderecoCesto);
        arqCestos.read(ba);
        c.fromByteArray(ba);
        
        // remove a chave
        if(!c.remove(chave))
            return false;
        
        // Atualiza o cesto
        arqCestos.seek(enderecoCesto);
        arqCestos.write(c.toByteArray());
        return true;
    }
    
    public void imprime() {
        try {
            byte[] bd = new byte[(int)arqDiretorio.length()];
            arqDiretorio.seek(0);
            arqDiretorio.read(bd);
            diretorio = new Diretorio();
            diretorio.fromByteArray(bd);   
            System.out.println("\nDIREToRIO ------------------");
            System.out.println(diretorio);

            System.out.println("\nCESTOS ---------------------");
            arqCestos.seek(0);
            while(arqCestos.getFilePointer() != arqCestos.length()) {
                Cesto c = new Cesto(quantidadeDadosPorCesto);
                byte[] ba = new byte[c.tamanho()];
                arqCestos.read(ba);
                c.fromByteArray(ba);
                System.out.println(c);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
}
