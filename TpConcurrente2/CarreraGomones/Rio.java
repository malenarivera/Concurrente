package CarreraGomones;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

//no van a poder hacer acquire de nuevos gomones si no terminaron 
public class Rio {
    private int cantIndividuales, cantDobles;//La cantidad de gomones que hay para cada tipo
    private int cantGomonesQueSePuedenTirar;//Cantidad de gomones que se pueden tirar juntos
    private Semaphore gomonesIndividuales, gomonesDobles;//Representa los gomones que hay
    private Semaphore mutex;//Para exclusion mutua
    private Semaphore ganador;//Para saber quien gano
    private Semaphore mandarSimple;//Rendevous entre clientes y gomones
    private Semaphore mandarDoble;//Rendevous entre clientes y gomones
    private Semaphore clientesBajarse;//Rendevous para avisar a los clientes que pueden bajarse 

    private CyclicBarrier barrera, meta;//Representan las lineas de meta y de llegada 
    private int cantIndActual, cantDoblesActual;//La cantidad de cada tipo de gomon que se tiro en un juego 


    private Thread reseteadorMeta;

    public Rio(int cantIndivuales, int cantDobles, int cantGomonesQueSePuedenTirar) {

        this.cantIndividuales = cantIndivuales;
        this.cantDobles = cantDobles;
        this.cantGomonesQueSePuedenTirar = cantGomonesQueSePuedenTirar;

        gomonesIndividuales = new Semaphore(this.cantIndividuales);
        gomonesDobles = new Semaphore(this.cantDobles * 2);
        mutex = new Semaphore(1);
        clientesBajarse = new Semaphore(0);
        ganador = new Semaphore(1);
        mandarSimple = new Semaphore(0);
        mandarDoble = new Semaphore(0);
    

        reseteadorMeta= new Thread(new Reseteador(this));
       

        barrera = new CyclicBarrier(cantGomonesQueSePuedenTirar);
        meta = new CyclicBarrier(cantGomonesQueSePuedenTirar, reseteadorMeta);

       

    }

    // metodos cliente
    public void liberarGomon() {
        try {
            Random r = new Random();
            if (r.nextInt(2) % 2 == 0) {

                gomonesIndividuales.acquire();
                System.out.println(Thread.currentThread().getName() + " se subio a uno" +
                        " indivual");

                mandarSimple.release();

            } else {

                gomonesDobles.acquire();

                System.out.println(Thread.currentThread().getName() + " se subio a uno doble");
                mandarDoble.release();
            }
        } catch (InterruptedException e) {

        }

    }

    public void bajarse() {
        try {
            clientesBajarse.acquire();

            System.out.println(Thread.currentThread().getName() + " se bajo");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // metodos de los gomones

    public void correrCarrera(int tipo) {
        try {
            if (tipo == 1) {
                mandarSimple.acquire();

                // System.out.println(Thread.currentThread().getName() + "se tomo un
                // individual");

            } else {
                mandarDoble.acquire(2);

                // System.out.println(Thread.currentThread().getName() + "se tomo uno doble ");

            }

            //puedenTirarse.acquire();
            barrera.await();

            mutex.acquire();

            if (tipo == 1) {
                cantIndActual++;

            } else {
                cantDoblesActual++;

            }

            mutex.release();

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void finalizarCarrera() {
        try {
            if (ganador.tryAcquire()) {
                System.out.println(Thread.currentThread().getName() + " GANO !!!!!!!!!!!!!!!!!!!!1");

            }
            meta.await();

        } catch (Exception e) {
            // TODO: handle exception
        }

    }
/*
    public void resetearBarrera() {

        System.out.println("-------------------SE RESETEA LA BARRERA-------------------");
        barrera.reset();

    }*/

    public void resetearJuego() {
    
        try {
            meta.reset();
            mutex.acquire();
                gomonesIndividuales.release(cantIndActual);
                gomonesDobles.release(cantDoblesActual * 2);
                clientesBajarse.release((cantIndActual + (cantDoblesActual * 2)));
                cantIndActual = 0;
                cantDoblesActual = 0;
            mutex.release();
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
        ganador.release();
               

    }

}
