package com.bolsadeideas.springboot.reactor.app.models;

public class Usuario {
    private String nombre;
    private String apellido;

    // Constructor
    public Usuario(String nombre, String apellido) {
        this.nombre = nombre;
        this.apellido = apellido;
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    // Método toString
    @Override
    public String toString() {
        return "Usuario {nombre='" + nombre + "', apellido='" + apellido + "'}";
    }
}
