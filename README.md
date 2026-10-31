# Google TV BT Remote 📱📺

Aplicativo Android nativo de alto desempenho desenvolvido com **Kotlin**, **Jetpack Compose** e **Bluetooth HID Device Profile** (`BluetoothHidDevice`), transformando o smartphone em um controle remoto físico Bluetooth sem fio para **Chromecast com Google TV**, **NVIDIA SHIELD**, **Mi Box / Xiaomi TV Stick** e **Android TV**.

---

## 🎨 Design Fiel ao Controle Oficial do Google TV

Baseado na interface oficial escura e minimalista:
- **Dark Mode Moderno:** Fundo `#141416`, botões em `#2A2A2E`, botão OK/Select em `#44444A`.
- **TopAppBar:** Botão `X` (fechar/desconectar), status e nome da TV conectada, botão `Power` e menu `⋮`.
- **2 Modos Centrais Alternáveis (menu `⋮`):**
  1. **Modo D-Pad:** Círculo direcional clássico com setas (`^`, `v`, `<`, `>`) e botão central `OK / Enter`.
  2. **Modo Touchpad (Swipe):** Superfície contínua de navegação por gestos com indicador circular branco que acompanha o toque. Gestos de swipe (cima, baixo, esquerda, direita) disparam a navegação e toques simples acionam `OK / Enter`.
- **Navegação do Sistema:** Botões circulares dedicados para **Voltar**, **Home** e **Google Assistente** (círculo branco com o ícone de 4 pontos do Assistente).
- **Controle de Áudio:** Botão circular de **Mudo** e pílula dupla com divisor fino para **Volume Down (-)** e **Volume Up (+)**.
- **Feedback Háptico:** Vibração tátil tátil a cada clique ou gesto reconhecido.

---

## ⚙️ Arquitetura Bluetooth HID de Baixo Nível

- **Perfil:** `BluetoothProfile.HID_DEVICE` (`BluetoothHidDevice`) introduzido na API 28 (Android 9+).
- **Descritor Composto (Composite HID Report Descriptor):**
  - **Report ID 1 (Standard Keyboard - Usage Page 0x01, Usage 0x06):**
    - Envia teclas de navegação rápida: `0x52` (Up), `0x51` (Down), `0x50` (Left), `0x4F` (Right) e `0x28` (Enter/OK).
  - **Report ID 2 (Consumer Control - Usage Page 0x0C, Usage 0x01):**
    - Envia comandos nativos de controle de mídia e sistema em formato Little Endian 16-bit:
      - `0x0223`: AC Home
      - `0x0224`: AC Back
      - `0x0221`: AC Search (Google Assistente)
      - `0x00E9`: Volume Increment
      - `0x00EA`: Volume Decrement
      - `0x00E2`: Mute
      - `0x0030`: Power / Sleep
- **Prevenção de Repetição Infinita (Key Release):** A função `sendKey` envia o payload do relatório de pressionamento e, após 30ms, envia o payload zerado de liberação (`release`), garantindo resposta rápida sem travamentos.
- **Reconexão Automática:** Persistência do último endereço MAC no `SharedPreferences` para reconexão imediata ao iniciar ou voltar para o app.
- **Gerenciamento de Dispositivos:** `ModalBottomSheet` para visualização e conexão direta de aparelhos já pareados.

---

## 📋 Como Parear com o Google TV / Android TV

1. **Na TV:**
   - Acesse **Configurações > Controles remotos e acessórios > Adicionar acessório**.
   - A TV começará a buscar novos dispositivos Bluetooth.
2. **No Smartphone:**
   - Abra o **Google TV BT Remote**.
   - No menu superior `⋮`, toque em **"Tornar Visível para Parear"** (ou abra as configurações Bluetooth).
   - O nome do smartphone aparecerá na tela da TV.
3. **Conclusão:**
   - Selecione o smartphone na TV e confirme o pareamento em ambas as telas.
   - O aplicativo conectará automaticamente e exibirá o nome da TV no topo com indicador verde.

---

## 🛠️ Tecnologias e Dependências

- **Linguagem:** Kotlin 2.0
- **UI:** Jetpack Compose (Material 3)
- **Min SDK:** 28 (Android 9 Pie)
- **Target SDK:** 34 (Android 14)
- **Compilador:** AGP 8.5.2 + Gradle 8.10.2
