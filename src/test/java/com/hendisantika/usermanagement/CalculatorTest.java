package com.hendisantika.usermanagement;

import com.hendisantika.usermanagement.service.Calculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Calculator tests - cas normaux, limites et exceptions")
class CalculatorTest {

    private final Calculator calc = new Calculator();

    // ===== Cas normaux =====
    @Test
    @DisplayName("Addition - cas normal")
    void addNormal() {
        assertEquals(5, calc.add(2, 3));
        assertEquals(-1, calc.add(2, -3));
    }

    @Test
    @DisplayName("Soustraction - cas normal")
    void subtractNormal() {
        assertEquals(2, calc.subtract(5, 3));
        assertEquals(8, calc.subtract(5, -3));
    }

    @Test
    @DisplayName("Multiplication - cas normal")
    void multiplyNormal() {
        assertEquals(20, calc.multiply(4, 5));
        assertEquals(-20, calc.multiply(4, -5));
    }

    @Test
    @DisplayName("Division - cas normal")
    void divideNormal() {
        assertEquals(5, calc.divide(10, 2));
        assertEquals(-5, calc.divide(-10, 2));
    }

    // ===== Cas limites =====
    @Test
    @DisplayName("Addition avec zéro et grands nombres (limite sans overflow)")
    void addEdgeCases() {
        assertEquals(Integer.MAX_VALUE, calc.add(Integer.MAX_VALUE, 0));
        assertEquals(2_000_000_000, calc.add(1_000_000_000, 1_000_000_000));
    }

    @Test
    @DisplayName("Soustraction menant à négatif / valeurs extrêmes")
    void subtractEdgeCases() {
        assertEquals(-5, calc.subtract(0, 5));
        assertEquals(Integer.MIN_VALUE, calc.subtract(Integer.MIN_VALUE, 0));
    }

    @Test
    @DisplayName("Multiplication par zéro et par un")
    void multiplyEdgeCases() {
        assertEquals(0, calc.multiply(123456, 0));
        assertEquals(123456, calc.multiply(123456, 1));
    }

    @Test
    @DisplayName("Division par 1 et division de 0")
    void divideEdgeCases() {
        assertEquals(42, calc.divide(42, 1));
        assertEquals(0, calc.divide(0, 5));
    }

    // ===== Exceptions =====
    @Test
    @DisplayName("Division par zéro doit lever ArithmeticException")
    void divideByZeroThrows() {
        assertThrows(ArithmeticException.class, () -> calc.divide(1, 0));
    }

    @Test
    @DisplayName("Vérification cohérence pour division entière (arrondi/troncature)")
    void divideIntegerBehavior() {
        // comportement attendu: division entière (troncature vers zéro)
        assertEquals(2, calc.divide(5, 2));   // 5/2 == 2
        assertEquals(-2, calc.divide(-5, 2)); // -5/2 == -2
    }
}