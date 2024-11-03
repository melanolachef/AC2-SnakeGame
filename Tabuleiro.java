import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

public class Tabuleiro extends JFrame {

    private JPanel painel;
    private JPanel menu;
    private JButton iniciarButton;
    private JButton resetButton;
    private JButton pauseButton;
    private JTextField placarField;
    private ArrayList<Quadrado> cobra;
    private Quadrado obstaculo;
    private int larguraTabuleiro = 400;
    private int alturaTabuleiro = 400;
    private int placar = 0;
    private String direcao = "direita";
    private int dificuldade = 100;
    private boolean jogoEmAndamento = false;
    private boolean pausado = false;
    private Timer timer;
    private int modoJogo = 1; // 1 ou 2

    public Tabuleiro() {
        iniciarUI();
    }

    // Método para configurar a interface do jogo
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

        // Botões para escolher o modo de jogo
        String[] opcoes = {"Modo 1 (Colidir)", "Modo 2 (Ressurgir)"};
        JComboBox<String> modoJogoBox = new JComboBox<>(opcoes);
        modoJogoBox.addActionListener(e -> modoJogo = modoJogoBox.getSelectedIndex() + 1);

        menu.add(modoJogoBox);
        menu.add(iniciarButton);
        menu.add(resetButton);
        menu.add(pauseButton);
        menu.add(placarField);

        // Configuração do painel principal
        painel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (cobra != null) {  // Verifica se cobra foi inicializada
                    for (Quadrado segmento : cobra) {
                        segmento.desenhar(g);
                    }
                }
                if (obstaculo != null) {
                    obstaculo.desenhar(g);
                }

                // Adicionando a mensagem na parte inferior
                g.setColor(Color.BLACK);
                g.drawString("Feito por Lucas e Guilherme", 10, alturaTabuleiro + 30);
            }
        };

        add(menu, BorderLayout.NORTH);
        add(painel, BorderLayout.CENTER);
        setVisible(true);

        // Configurações dos botões
        iniciarButton.addActionListener(e -> iniciarJogo());
        resetButton.addActionListener(e -> reiniciarJogo());
        pauseButton.addActionListener(e -> pausarJogo());

        // Controle de movimento com W, A, S, D
        painel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A -> direcao = !direcao.equals("direita") ? "esquerda" : direcao;
                    case KeyEvent.VK_D -> direcao = !direcao.equals("esquerda") ? "direita" : direcao;
                    case KeyEvent.VK_W -> direcao = !direcao.equals("baixo") ? "cima" : direcao;
                    case KeyEvent.VK_S -> direcao = !direcao.equals("cima") ? "baixo" : direcao;
                }
            }
        });

        painel.setFocusable(true);
        painel.requestFocusInWindow();
    }

    // Método para iniciar o jogo
    private void iniciarJogo() {
        cobra = new ArrayList<>();
        cobra.add(new Quadrado(larguraTabuleiro / 2, alturaTabuleiro / 2, 10, 10, Color.BLACK));
        direcao = "direita";
        placar = 0;
        dificuldade = 100;
        placarField.setText("Placar: 0");
        criarObstaculo();

        jogoEmAndamento = true;
        pausado = false;

        if (timer != null) timer.stop();
        timer = new Timer(dificuldade, e -> atualizarJogo());
        timer.start();

        painel.requestFocusInWindow();  // Garante que o painel tenha o foco para capturar teclas
    }

    // Método para atualizar o estado do jogo a cada tick do timer
    private void atualizarJogo() {
        if (!jogoEmAndamento || pausado) return;

        moverCobra();
        checarColisao();
        painel.repaint();
    }

    // Método para mover a cobra
    private void moverCobra() {
        Quadrado cabeca = cobra.get(0);
        int novoX = cabeca.x, novoY = cabeca.y;

        switch (direcao) {
            case "esquerda" -> novoX -= 10;
            case "direita" -> novoX += 10;
            case "cima" -> novoY -= 10;
            case "baixo" -> novoY += 10;
        }

        // Movendo o corpo da cobra
        for (int i = cobra.size() - 1; i > 0; i--) {
            cobra.get(i).x = cobra.get(i - 1).x;
            cobra.get(i).y = cobra.get(i - 1).y;
        }

        cobra.get(0).x = novoX;
        cobra.get(0).y = novoY;

        // Aumenta a velocidade a cada maçã
        if (cobra.get(0).x == obstaculo.x && cobra.get(0).y == obstaculo.y) {
            aumentarPontuacao();
            criarObstaculo();
            cobra.add(new Quadrado(-10, -10, 10, 10, Color.BLACK)); // Novo segmento fora da tela temporariamente

            dificuldade = Math.max(20, dificuldade - 5); // Reduz o tempo do timer para aumentar a velocidade
            timer.setDelay(dificuldade);
        }
    }

    // Método para verificar colisões
    private void checarColisao() {
        Quadrado cabeca = cobra.get(0);

        // Colisão com as bordas
        if (modoJogo == 1) { // Modo 1: Colidir
            if (cabeca.x < 0 || cabeca.x >= larguraTabuleiro || cabeca.y < 0 || cabeca.y >= alturaTabuleiro) {
                jogoEmAndamento = false;
                JOptionPane.showMessageDialog(this, "Você perdeu! Pontuação: " + placar, "Game Over", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        } else { // Modo 2: Ressurgir
            if (cabeca.x < 0) cabeca.x = larguraTabuleiro - 10;
            if (cabeca.x >= larguraTabuleiro) cabeca.x = 0;
            if (cabeca.y < 0) cabeca.y = alturaTabuleiro - 10;
            if (cabeca.y >= alturaTabuleiro) cabeca.y = 0;
        }

        // Colisão com o próprio corpo
        for (int i = 1; i < cobra.size(); i++) {
            if (cobra.get(i).x == cabeca.x && cobra.get(i).y == cabeca.y) {
                jogoEmAndamento = false;
                JOptionPane.showMessageDialog(this, "Você perdeu! Pontuação: " + placar, "Game Over", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
    }

    // Método para reiniciar o jogo
    private void reiniciarJogo() {
        jogoEmAndamento = false;
        pausado = false;
        if (timer != null) timer.stop();
        iniciarJogo();
    }

    // Método para pausar o jogo
    private void pausarJogo() {
        pausado = !pausado;
        if (pausado) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Jogo Pausado!", "Pause", JOptionPane.INFORMATION_MESSAGE);
        } else {
            timer.start();
        }
    }

    // Método para criar um novo obstáculo (maçã)
    private void criarObstaculo() {
        Random rand = new Random();
        // Garante que a maçã apareça dentro dos limites da cobra
        int x = (rand.nextInt(larguraTabuleiro / 10) * 10);
        int y = (rand.nextInt(alturaTabuleiro / 10) * 10);
        obstaculo = new Quadrado(x, y, 10, 10, Color.RED);
    }

    // Método para aumentar a pontuação
    private void aumentarPontuacao() {
        placar++;
        placarField.setText("Placar: " + placar);
    }

    public static void main(String[] args) {
        new Tabuleiro();
    }
}
