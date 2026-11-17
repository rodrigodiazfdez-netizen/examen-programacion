import java.util.Random;
import java.util.Scanner;

/** La Mazmorra Infinita: aventura procedural de consola. */
class MazmorraInfinita {
    private static final Scanner SC = new Scanner(System.in);
    private static final Random RNG = new Random();
    private static final char VACIA='.', COMUN='E', ELITE='X', TRAMPA='T', AVANZADA='A',
            COFRE='C', CURA='H', COMERCIANTE='M', ACERTIJO='?', OBSTACULO='#', LLAVE='K', TESORO='$';
    private static int vidasIniciales=4, tamano=8, dificultad=2;

    public static void main(String[] args) {
        System.out.println("=== LA MAZMORRA INFINITA ===");
        boolean salir=false;
        while (!salir) {
            System.out.println("\n1. Nueva Partida\n2. Configuración\n3. Instrucciones\n4. Créditos\n5. Salir");
            int op=leerOpcion("Seleccione una opción: ",1,5);
            switch(op) {
                case 1: jugarConReintento(); break;
                case 2: configuracion(); break;
                case 3: instrucciones(); break;
                case 4: System.out.println("\nProyecto: La Mazmorra Infinita – Aventura Procedural.\nProgramación en Java, modo consola."); break;
                case 5: salir=true; System.out.println("¡Gracias por jugar!"); break;
                default: break;
            }
        }
    }

    private static void jugarConReintento() {
        boolean otra=true;
        while(otra) {
            Resultado r=partidaNueva();
            System.out.println(r==Resultado.VICTORIA ? "\n¡VICTORIA! Has encontrado el Tesoro Supremo." :
                    r==Resultado.DERROTA ? "\nDERROTA: te has quedado sin vidas." : "\nHas abandonado la aventura.");
            otra=leerOpcion("¿Desea jugar otra vez? 1. Sí  2. No: ",1,2)==1;
        }
    }

    private static void configuracion() {
        boolean volver=false;
        while(!volver) {
            System.out.println("\n=== CONFIGURACIÓN ===\n1. Vidas iniciales (actual: "+vidasIniciales+")\n2. Tamaño del mapa (actual: "+tamano+"x"+tamano+")\n3. Dificultad (actual: "+nombreDificultad()+")\n4. Volver");
            switch(leerOpcion("Opción: ",1,4)) {
                case 1: System.out.println("Elige 3, 4 o 5 vidas:"); vidasIniciales=leerEntreValores(3,4,5); break;
                case 2: System.out.println("Tamaño: 1) 6x6  2) 8x8  3) 10x10"); tamano=leerOpcion("Opción: ",1,3)*2+4; break;
                case 3: System.out.println("Dificultad: 1) Fácil  2) Normal  3) Difícil"); dificultad=leerOpcion("Opción: ",1,3); break;
                case 4: volver=true; break;
            }
        }
    }

    private static String nombreDificultad() { return dificultad==1?"Fácil":dificultad==2?"Normal":"Difícil"; }
    private static void instrucciones() {
        System.out.println("\nExplora con las flechas numéricas del menú. Encuentra la Llave Mística (K) y luego el Tesoro Supremo ($).\n"+
                "Los obstáculos (#) no se pueden atravesar. Enemigos y trampas causan daño; el oro sirve en comercios.\n"+
                "Puedes consultar tu estado y usar pociones desde el menú de acciones. La posición inicial es segura.");
    }

    private enum Resultado { VICTORIA, DERROTA, ABANDONO }
    private static class Jugador {
        int fila,col,vidas,oro=0,pociones=1,turnos=0,veneno=0,escudo=0;
        boolean llave=false, pista=false;
    }

    private static Resultado partidaNueva() {
        char[][] mapa=generarMapa();
        Jugador j=new Jugador(); j.fila=0; j.col=0; j.vidas=vidasIniciales;
        System.out.println("\nNueva mazmorra "+tamano+"x"+tamano+" | "+nombreDificultad()+". Usa 1-4 para moverte.");
        while(j.vidas>0) {
            mostrarMapa(mapa,j);
            mostrarEstadoBreve(j);
            System.out.println("\n=== ACCIONES ===\n1. Mover arriba\n2. Mover abajo\n3. Mover izquierda\n4. Mover derecha\n5. Ver estado del jugador\n6. Usar objeto/curación\n7. Abandonar partida");
            int op=leerOpcion("Seleccione una opción: ",1,7);
            if(op==5) { mostrarEstado(j); continue; }
            if(op==6) { usarObjeto(j); continue; }
            if(op==7) return Resultado.ABANDONO;
            try { mover(j,mapa,op); }
            catch(IllegalArgumentException ex) { System.out.println(ex.getMessage()); continue; }
            j.turnos++;
            if(j.veneno>0) { recibirDanio(j,1,"El veneno te hace daño"); j.veneno--; }
            if(j.vidas<=0) return Resultado.DERROTA;
            char evento=mapa[j.fila][j.col]; mapa[j.fila][j.col]=VACIA;
            Resultado res=procesarEvento(evento,j);
            if(res==Resultado.VICTORIA) return res;
        }
        return Resultado.DERROTA;
    }

    /** Genera una matriz nueva en cada partida, distribuye eventos según la dificultad y reserva el inicio. */
    private static char[][] generarMapa() {
        char[][] m=new char[tamano][tamano];
        for(int f=0;f<tamano;f++) for(int c=0;c<tamano;c++) m[f][c]=VACIA;
        int celdas=tamano*tamano;
        colocar(m,OBSTACULO,celdas/14);
        colocar(m,COMUN,celdas/10 + dificultad);
        colocar(m,ELITE,1+dificultad);
        colocar(m,TRAMPA,celdas/12 + dificultad);
        colocar(m,AVANZADA,dificultad);
        colocar(m,COFRE,celdas/18+2);
        colocar(m,CURA,Math.max(2,4-dificultad));
        colocar(m,COMERCIANTE,2);
        colocar(m,ACERTIJO,2);
        colocar(m,LLAVE,1);
        colocar(m,TESORO,1);
        return m;
    }
    private static void colocar(char[][] m,char tipo,int cantidad) {
        int intentos=0, limite=tamano*tamano*20;
        while(cantidad>0 && intentos++<limite) {
            int f=RNG.nextInt(tamano),c=RNG.nextInt(tamano);
            // La salida queda siempre despejada y se evita apilar eventos.
            if((f==0&&c==0)||m[f][c]!=VACIA) continue;
            // Mantiene accesibles la llave y el tesoro; los obstáculos no los pueden rodear por diseño.
            if(tipo==OBSTACULO && (f==0||c==0||f==tamano-1||c==tamano-1)) continue;
            m[f][c]=tipo; cantidad--;
        }
    }

    /** Valida límites y obstáculos antes de modificar la posición del jugador. */
    private static void mover(Jugador j,char[][] m,int op) {
        int nf=j.fila,nc=j.col;
        if(op==1) nf--; else if(op==2) nf++; else if(op==3) nc--; else if(op==4) nc++;
        if(nf<0||nf>=tamano||nc<0||nc>=tamano) throw new IllegalArgumentException("Movimiento inválido: no puedes salir del mapa.");
        if(m[nf][nc]==OBSTACULO) throw new IllegalArgumentException("Un obstáculo bloquea ese camino.");
        j.fila=nf; j.col=nc;
    }

    /** Cada evento se consume una vez al entrar en la casilla. */
    private static Resultado procesarEvento(char e,Jugador j) {
        switch(e) {
            case COMUN:
                System.out.println("¡Un enemigo común te ataca!"); recibirDanio(j,1+RNG.nextInt(2),"El enemigo común");
                if(j.vidas>0) { int botin=5+RNG.nextInt(11); j.oro+=botin; System.out.println("Obtienes "+botin+" monedas."); } break;
            case ELITE:
                System.out.println("¡Enemigo élite!"); int d=2+RNG.nextInt(2); if(RNG.nextInt(100)<25) { d+=2; System.out.println("¡Golpe crítico!"); }
                recibirDanio(j,d,"El enemigo élite"); if(j.vidas>0) { j.oro+=15; System.out.println("Recompensa: 15 monedas."); } break;
            case TRAMPA: recibirDanio(j,2,"Una trampa"); break;
            case AVANZADA:
                recibirDanio(j,1+RNG.nextInt(3),"Una trampa avanzada");
                if(j.vidas>0&&RNG.nextBoolean()) { j.veneno=3; System.out.println("¡Has quedado envenenado durante 3 turnos!"); } break;
            case CURA: int curada=Math.min(2,vidasIniciales-j.vidas); j.vidas+=curada; System.out.println(curada>0?"Recuperas "+curada+" vidas.":"Encuentras una hierba, pero ya tienes la vida al máximo."); break;
            case COFRE: abrirCofre(j); break;
            case COMERCIANTE: comerciante(j); break;
            case ACERTIJO: acertijo(j); break;
            case LLAVE: j.llave=true; System.out.println("¡Has conseguido la Llave Mística!"); break;
            case TESORO:
                if(j.llave) return Resultado.VICTORIA;
                System.out.println("Encuentras el Tesoro Supremo, pero necesitas la Llave Mística. La ubicación queda marcada en tu mapa."); j.pista=true; break;
            default: System.out.println("La sala está vacía.");
        }
        return j.vidas<=0?Resultado.DERROTA:null;
    }

    private static void abrirCofre(Jugador j) {
        int suerte=RNG.nextInt(5);
        if(suerte==0) { recibirDanio(j,2,"Una trampa oculta del cofre"); }
        else if(suerte==1) { j.pociones++; System.out.println("El cofre contiene una poción."); }
        else if(suerte==2) { j.escudo=3; System.out.println("Obtienes un escudo: reduce 1 de daño durante 3 ataques."); }
        else if(suerte==3) { int g=10+RNG.nextInt(21); j.oro+=g; System.out.println("Encuentras "+g+" monedas."); }
        else { int cura=Math.min(2,vidasIniciales-j.vidas); j.vidas+=cura; System.out.println(cura>0?"Encuentras una poción curativa.":"El cofre está vacío."); }
    }
    private static void comerciante(Jugador j) {
        boolean fin=false;
        while(!fin) {
            System.out.println("\nComerciante (oro: "+j.oro+")\n1. Poción curativa (12)\n2. Escudo temporal (18)\n3. Pista sobre la llave (10)\n4. Salir");
            int op=leerOpcion("¿Qué deseas? ",1,4);
            if(op==4) fin=true;
            else if(op==1) { if(j.oro>=12) { j.oro-=12;j.pociones++;System.out.println("Poción comprada."); } else System.out.println("No tienes suficiente oro."); }
            else if(op==2) { if(j.oro>=18) { j.oro-=18;j.escudo+=5;System.out.println("El escudo durará 5 ataques."); } else System.out.println("No tienes suficiente oro."); }
            else if(op==3) { if(j.oro>=10) { j.oro-=10;j.pista=true;System.out.println("La Llave Mística está en algún lugar de la mazmorra. Explora las casillas sin visitar."); } else System.out.println("No tienes suficiente oro."); }
        }
    }
    private static void acertijo(Jugador j) {
        System.out.println("Acertijo: ¿Cuántos lados tiene un hexágono? (escribe un número)");
        String r=SC.nextLine().trim();
        if(r.equals("6")) { j.oro+=12; j.pociones++; System.out.println("¡Correcto! Ganas 12 monedas y una poción."); }
        else { System.out.println("Respuesta incorrecta. La respuesta era 6."); recibirDanio(j,1,"El acertijo"); }
    }
    private static void usarObjeto(Jugador j) {
        System.out.println("\nObjetos: 1. Usar poción (tienes "+j.pociones+")  2. Volver");
        int op=leerOpcion("Opción: ",1,2);
        if(op==2) return;
        try {
            if(j.pociones<=0) throw new IllegalStateException("No tienes pociones disponibles.");
            if(j.vidas>=vidasIniciales) { System.out.println("Ya tienes todas tus vidas."); return; }
            j.pociones--; int cura=Math.min(2,vidasIniciales-j.vidas); j.vidas+=cura; j.veneno=0;
            System.out.println("Recuperas "+cura+" vidas y eliminas el veneno.");
        } catch(IllegalStateException ex) { System.out.println(ex.getMessage()); }
    }
    private static void recibirDanio(Jugador j,int cantidad,String causa) {
        int absorbido=0;
        if(j.escudo>0) { absorbido=Math.min(1,cantidad); j.escudo--; }
        int real=cantidad-absorbido; j.vidas=Math.max(0,j.vidas-real);
        System.out.println(causa+" causa "+real+" de daño. Vidas: "+j.vidas+"/"+vidasIniciales+".");
    }
    private static void mostrarEstadoBreve(Jugador j) { System.out.println("Vidas: "+j.vidas+" | Oro: "+j.oro+" | Pociones: "+j.pociones+(j.llave?" | Llave: sí":" | Llave: no")); }
    private static void mostrarEstado(Jugador j) {
        System.out.println("\n=== ESTADO ==="); mostrarEstadoBreve(j);
        System.out.println("Posición: fila "+(j.fila+1)+", columna "+(j.col+1)+" | Veneno: "+j.veneno+" turnos | Ataques protegidos: "+j.escudo+" | Turnos: "+j.turnos);
        if(j.pista) System.out.println("Pista: ya sabes que la llave está en la mazmorra; revisa el mapa y sigue explorando.");
    }
    private static void mostrarMapa(char[][] m,Jugador j) {
        System.out.println("\nMapa (?: evento desconocido; @: tú):");
        for(int f=0;f<tamano;f++) { for(int c=0;c<tamano;c++) {
            if(f==j.fila&&c==j.col) System.out.print("@ ");
            else { char x=m[f][c]; System.out.print((x==VACIA?"·":x)+" "); }
        } System.out.println(); }
        System.out.println("Leyenda: @ jugador, · vacío, #: obstáculo. Las demás casillas se revelan al visitarlas.");
    }

    /** Entrada numérica robusta: Scanner.nextInt no se deja bloqueado por texto inválido. */
    private static int leerOpcion(String mensaje,int min,int max) {
        while(true) {
            System.out.print(mensaje); String linea=SC.nextLine().trim();
            try {
                int n=Integer.parseInt(linea);
                if(n<min||n>max) throw new IllegalArgumentException("Opción inexistente. Elige un número entre "+min+" y "+max+".");
                return n;
            } catch(NumberFormatException ex) { System.out.println("Entrada inválida: escribe un número."); }
              catch(IllegalArgumentException ex) { System.out.println(ex.getMessage()); }
        }
    }
    private static int leerEntreValores(int... valores) {
        while(true) {
            String s=SC.nextLine().trim();
            try { int n=Integer.parseInt(s); for(int v:valores) if(n==v) return n; System.out.println("Elige uno de los valores indicados."); }
            catch(NumberFormatException ex) { System.out.println("Entrada inválida: escribe un número."); }
        }
    }
}
