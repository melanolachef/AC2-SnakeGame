import java.awt.Color;
import java.awt.Graphics;

// Classe que representa um segmento da cobra e as maçãs (obstáculos)
public class Quadrado {
    int x, y, largura, altura;
    Color cor;

    // Construtor completo com largura, altura e cor
    public Quadrado(int x, int y, int largura, int altura, Color cor) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.cor = cor;
    }

    // Construtor alternativo apenas com x e y, usando valores padrão para largura, altura e cor
    public Quadrado(int x, int y) {
        this(x, y, 10, 10, Color.BLACK); // Define largura e altura como 10, e cor como preto
    }

    public void desenhar(Graphics g) {
        g.setColor(cor);
        g.fillRect(x, y, largura, altura);
    }
}
