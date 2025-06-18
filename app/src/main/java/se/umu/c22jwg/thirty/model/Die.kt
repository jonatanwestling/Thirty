package se.umu.c22jwg.thirty.model

class Die {
    var value: Int = 1;
    var selected = false;

    fun roll() {
        value = (1..6).random();
    }
}