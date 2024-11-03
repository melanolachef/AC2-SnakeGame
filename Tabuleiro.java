import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class Tabuleiro extends JFrame {
    // Variáveis para os elementos do jogo
    private JPanel painel;
    private JPanel menu;
    private JButton iniciarButton;
    private JButton resetButton;
    private JButton pauseButton;
    private JTextField placarField;
    private ArrayList<Quadrado> cobra;
    private Quadrado obstaculo;
    private Quadrado macaVerde;
    private int larguraTabuleiro = 400;
    private int alturaTabuleiro = 400;
    private int placar = 0;
    private String direcao = "direita";
    private int dificuldade = 100;
    private boolean jogoEmAndamento = false;
    private boolean pausado = false;
    private Timer timer;
    private int modoJogo = 1; // 1 ou 2
    private boolean controlesInvertidos = false; // Controle do estado dos controles invertidos

    public Tabuleiro() {
        iniciarUI();
    }

    private void iniciarUI() {
        setTitle("Jogo da Cobrinha");
        setSize(larguraTabuleiro, alturaTabuleiro + 50);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Configuração do menu
        menu = new JPanel(new FlowLayout());
        iniciarButton = new JButton("Iniciar");
        resetButton = new JButton("Reiniciar");
        pauseButton = new JButton("Pausar");
        placarField = new JTextField("Placar: 0", 10);
        placarField.setEditable(false);

        String[] opcoes = {"Modo 1 (Colidir)", "Modo 2 (Ressurgir)"};
        JComboBox<String> modoJogoBox = new JComboBox<>(opcoes);
        modoJogoBox.addActionListener(e -> modoJogo = modoJogoBox.getSelectedIndex() + 1);

        menu.add(modoJogoBox);
        menu.add(iniciarButton);
        menu.add(resetButton);
        menu.add(pauseButton);
        menu.add(placarField);

        painel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLACK); // Cor da borda
                g.drawRect(0, 0, larguraTabuleiro - 1, alturaTabuleiro - 1); // Desenha a borda
                if (cobra != null) {
                    for (Quadrado segmento : cobra) {
                        segmento.desenhar(g);
                    }
                }
                if (obstaculo != null) {
                    obstaculo.desenhar(g);
                }
                if (macaVerde != null) {
                    macaVerde.desenhar(g);
                }
            }
        };

        add(menu, BorderLayout.NORTH);
        add(painel, BorderLayout.CENTER);
        setVisible(true);

        iniciarButton.addActionListener(e -> iniciarJogo());
        resetButton.addActionListener(e -> reiniciarJogo());
        pauseButton.addActionListener(e -> pausarJogo());

        painel.setFocusable(true);
        painel.requestFocusInWindow();

        // Adiciona o KeyListener para o painel
        painel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A -> direcao = controlesInvertidos ? "direita" : "esquerda";
                    case KeyEvent.VK_D -> direcao = controlesInvertidos ? "esquerda" : "direita";
                    case KeyEvent.VK_W -> direcao = controlesInvertidos ? "baixo" : "cima";
                    case KeyEvent.VK_S -> direcao = controlesInvertidos ? "cima" : "baixo";
                }
            }
        });
    }

    private void iniciarJogo() {
        cobra = new ArrayList<>();
        cobra.add(new Quadrado(larguraTabuleiro / 2, alturaTabuleiro / 2, 10, 10, Color.BLACK));
        direcao = "direita";
        placar = 0;
        dificuldade = 100;
        controlesInvertidos = false;
        placarField.setText("Placar: 0");
        criarObstaculo();
        macaVerde = null;

        jogoEmAndamento = true;
        pausado = false;

        if (timer != null) timer.stop();
        timer = new Timer(dificuldade, e -> atualizarJogo());
        timer.start();

        painel.requestFocusInWindow();
    }

    private void aumentarPontuacao() {
        placar++;
        placarField.setText("Placar: " + placar);
    }

    private void criarObstaculo() {
        Random random = new Random();
        int x = random.nextInt(larguraTabuleiro / 10) * 10;
        int y = random.nextInt(alturaTabuleiro / 10) * 10;
        obstaculo = new Quadrado(x, y, 10, 10, Color.RED);
    }

    private void reiniciarJogo() {
        iniciarJogo();
    }

    private void pausarJogo() {
        if (jogoEmAndamento) {
            pausado = !pausado;
            if (pausado) {
                timer.stop();
            } else {
                timer.start();
            }
        }
    }

    private void atualizarJogo() {
        if (!jogoEmAndamento || pausado) return;

        moverCobra();
        checarColisao();
        painel.repaint();
    }

    private void moverCobra() {
        // Obtém a cabeça da cobra
        Quadrado cabeca = cobra.get(0);
        int novoX = cabeca.x;
        int novoY = cabeca.y;

        // Ajusta a posição com base na direção
        switch (direcao) {
            case "esquerda" -> {
                if (!direcao.equals("direita")) novoX -= 10;
            }
            case "direita" -> {
                if (!direcao.equals("esquerda")) novoX += 10;
            }
            case "cima" -> {
                if (!direcao.equals("baixo")) novoY -= 10;
            }
            case "baixo" -> {
                if (!direcao.equals("cima")) novoY += 10;
            }
        }

        // Move cada segmento da cobra para a posição do segmento anterior
        for (int i = cobra.size() - 1; i > 0; i--) {
            cobra.get(i).x = cobra.get(i - 1).x;
            cobra.get(i).y = cobra.get(i - 1).y;
        }

        // Atualiza a posição da cabeça
        cobra.get(0).x = novoX;
        cobra.get(0).y = novoY;

        // Verifica se a cobra comeu uma maçã
        if (cabeca.x == obstaculo.x && cabeca.y == obstaculo.y) {
            aumentarPontuacao();
            criarObstaculo();
            cobra.add(new Quadrado(-10, -10, 10, 10, Color.BLACK)); // Adiciona um novo segmento
            dificuldade = Math.max(20, dificuldade - 5); // Aumenta a velocidade
            timer.setDelay(dificuldade);
            // Se a maçã vermelha foi comida, reverte os controles ao normal
            controlesInvertidos = false;

            // Gera a maçã verde a cada 10 pontos
            if (placar % 10 == 0) {
                gerarMacaVerde();
            }
        } else if (macaVerde != null && cabeca.x == macaVerde.x && cabeca.y == macaVerde.y) {
            macaVerde = null; // Remove a maçã verde após ser comida
            if (cobra.size() > 1) {
                cobra.remove(cobra.size() - 1); // Reduz o tamanho da cobra
            }
            controlesInvertidos = true; // Inverte os controles
        }
    }

    private void gerarMacaVerde() {
        Random random = new Random();
        int x, y;
        // Gera coordenadas que não colidam com a cobra ou o obstáculo
        do {
            x = random.nextInt(larguraTabuleiro / 10) * 10;
            y = random.nextInt(alturaTabuleiro / 10) * 10;
        } while (colidiuComCobra(x, y) || (obstaculo != null && obstaculo.x == x && obstaculo.y == y));
        macaVerde = new Quadrado(x, y, 10, 10, Color.GREEN); // Cria a maçã verde
    }

    private boolean colidiuComCobra(int x, int y) {
        for (Quadrado segmento : cobra) {
            if (segmento.x == x && segmento.y == y) {
                return true; // Colidiu com a cobra
            }
        }
        return false;
    }

    private void checarColisao() {
        Quadrado cabeca = cobra.get(0);

        // Colisão com as bordas
        if (modoJogo == 1) { // Modo 1: Colidir
            if (cabeca.x < 0 || cabeca.x >= larguraTabuleiro || cabeca.y < 0 || cabeca.y >= alturaTabuleiro) {
                gameOver();
            }
        } else { // Modo 2: Ressurgir
            if (cabeca.x < 0) cabeca.x = larguraTabuleiro - 10;
            if (cabeca.x >= larguraTabuleiro) cabeca.x = 0;
            if (cabeca.y < 0) cabeca.y = alturaTabuleiro - 10;
            if (cabeca.y >= alturaTabuleiro) cabeca.y = 0;
        }

        // Colisão com o corpo
        for (int i = 1; i < cobra.size(); i++) {
            if (cabeca.x == cobra.get(i).x && cabeca.y == cobra.get(i).y) {
                gameOver();
            }
        }
    }

    private void gameOver() {
        jogoEmAndamento = false;
        timer.stop();
        JOptionPane.showMessageDialog(this, "Game Over! Você fez " + placar + " pontos.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Tabuleiro::new);
    }
}


