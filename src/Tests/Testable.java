package Tests;

abstract public class Testable {

    protected int processNumber;

    public Testable(int processNumber) {

        this.processNumber = processNumber;
    }

    protected String getHttpPort(){

        return this.processNumber == 1 ? "8000" : this.processNumber == 2 ? "8001" : "8002";
    }
}
