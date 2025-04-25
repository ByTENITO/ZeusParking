# ZeusParking - Sistema de Gestión de Parqueaderos Universitarios

![Logo de la App](Logo.jpg) 

## Descripción del Proyecto

ZeusParking es una aplicación móvil desarrollada como proyecto académico para la gestión inteligente de parqueaderos en campus universitarios. La solución permite:

- Registro y autenticación segura de usuarios (estudiantes, personal administrativo y vigilantes)
- Control de acceso vehicular mediante tecnología QR
- Visualización en tiempo real de disponibilidad de espacios
- Notificaciones proactivas sobre capacidad del parqueadero
- Integración con Google Maps para ubicación de sedes

El sistema está diseñado para optimizar el flujo vehicular en las instalaciones de la universidad, reduciendo tiempos de espera y mejorando la seguridad.

## Características Principales

### Módulo de Usuario
- 🔐 Autenticación con correo electrónico (verificación por link) o cuenta Google
- 📝 Registro de vehículos (bicicletas, motos, carros, furgones)
- 🖼️ Almacenamiento de fotos del usuario y del vehículo
- 🏷️ Generación de QR único con datos del vehículo
- 📊 Visualización de espacios disponibles por tipo de vehículo
- 📌 Geolocalización de sedes universitarias

### Módulo de Vigilante
- 📷 Escáner QR para registrar entradas/salidas
- 👤 Validación visual de fotos del usuario y vehículo
- 🔄 Actualización en tiempo real de disponibilidad
- ⚠️ Notificaciones cuando los espacios están por agotarse

## Instrucciones de Instalación

1. **Requisitos previos:**
   - Android Studio Flamingo o superior
   - Dispositivo físico o emulador con Android 8.0 (Oreo) mínimo
   - Conexión a Internet para configuración inicial

2. **Pasos para ejecutar el proyecto:**
   ```bash
   git clone https://github.com/ByTENITO/ZeusParking
   cd ZeusParking
   Abrir en Android Studio y sincronizar Gradle
   Ejecutar en dispositivo/emulador
