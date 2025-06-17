package se.umu.c22jwg.thirty.model

class Die {
    private var value: Int = 0;

    fun roll(): Int {
        value = (1..6).random();
        return value;
    }
}