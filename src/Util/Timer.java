package Util;

public class Timer{

    private long timestamp;

    public void start(){

        this.timestamp = 1L + (long) (Math.random() * 3L);

        Thread timeUp = new Thread(new TimeUp(this));

        timeUp.start();
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void addTimestamp(){
        this.timestamp++;
    }

    static class TimeUp implements Runnable{

        private long start = 0;

        private long delay = 1000;

        private Timer timerInstance;

        public TimeUp(Timer timer)
        {

            this.timerInstance = timer;
        }

        @Override
        public void run() {

            this.start();

            while (true)
            {
                if(this.isExpired())
                {

                    this.timerInstance.addTimestamp();

                    this.start = System.currentTimeMillis();

                }
            }
        }

        private boolean isExpired(){

            return (System.currentTimeMillis() - this.start) > delay;
        }

        private void start(){

            this.start = System.currentTimeMillis();
        }
    }

}
