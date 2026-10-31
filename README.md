# 📺 Google TV BT Remote 📱

[![Android](https://img.shields.io/badge/Android-API%2028%2B%20(9.0%2B)-brightgreen.svg?logo=android)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34-blue.svg)](https://developer.android.com/about/versions/14)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple.svg?logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20(Material%203)-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Bluetooth HID](https://img.shields.io/badge/Protocol-Bluetooth%20HID%20Device-orange.svg?logo=bluetooth)](https://developer.android.com/reference/android/bluetooth/BluetoothHidDevice)

Transforme qualquer smartphone Android em um **controle remoto Bluetooth nativo** para **Chromecast com Google TV**, **Android TV**, **NVIDIA SHIELD** e **Xiaomi Mi Box**.

> 💡 **Como funciona:** O aplicativo utiliza o perfil de baixo nível **Bluetooth HID Device (`BluetoothHidDevice`)**, fazendo com que o seu celular seja reconhecido pela TV exatamente como um **controle remoto físico de verdade**. Não requer Wi-Fi, não precisa estar na mesma rede, não precisa de conta Google e não depende de comandos ADB.

---

## ⚡ Início Rápido (Instalação em 1 minuto)

Você não precisa compilar nada se só quiser usar o controle:

1. **Baixe o arquivo APK:**
   - O arquivo pronto para instalação já está disponível na raiz deste repositório:
   - 👉 **[`GoogleTVBTRemote-debug.apk`](GoogleTVBTRemote-debug.apk)** *(~53 MB)*
2. **Instale no celular:**
   - Transfira o APK para o celular (via WhatsApp, Google Drive, cabo USB ou Quick Share) e toque para instalar.
   - *Ou via terminal com o celular conectado:*
     ```bash
     adb install -r GoogleTVBTRemote-debug.apk
     ```
3. Abra o app e conceda a permissão de Bluetooth solicitada na primeira inicialização.

---

## 🔗 Como Parear com a sua TV (Passo a Passo)

> [!IMPORTANT]
> **Por que parear do celular para a TV?**  
> O Google TV aplica um filtro interno na tela dele para esconder smartphones e evitar poluição visual com celulares de vizinhos. Por isso, a conexão deve ser iniciada **pelo aplicativo no celular**.

### Passo 1: Prepare a TV
1. No Chromecast / Google TV, acesse:
   **Configurações ⚙️ > Controles remotos e acessórios > Adicionar acessório**.
2. Deixe a TV nessa tela (ela começará a buscar e ficará visível no Bluetooth).

### Passo 2: Localize a TV no aplicativo
1. Abra o **Google TV BT Remote** no smartphone.
2. Toque no topo da tela onde diz **"Desconectado - Toque para conectar"** (ou no menu superior `⋮ > Dispositivos Pareados`).
3. Toque no botão azul **"Buscar TV"**:
   - O aplicativo encontrará o seu Chromecast/TV e exibirá o nome dele destacado na lista.
4. **Toque no nome da sua TV**.

### Passo 3: Confirme o pareamento
1. Uma janela de confirmação de pareamento com código PIN aparecerá **na TV e no celular**.
2. Confirme o pareamento em ambos.
3. **Pronto!** O app mudará automaticamente para o status **Conectado (verde)** e você já pode controlar a TV.

---

## 🎛️ Funcionalidades e Layout Oficial

O design foi construído em **Dark Mode minimalista** reproduzindo o controle oficial do Google TV:

```
┌───────────────────────────────────────┐
│  [X] Chromecast                 (⏻) [⋮]│ ◄── TopBar com status e menu
├───────────────────────────────────────┤
│                                       │
│          ┌─────────────────┐          │
│          │        ▲        │          │
│          │    ◄   ●   ►    │          │ ◄── Alternável no menu [⋮]:
│          │        ▼        │          │     • Modo D-Pad (Círculo)
│          └─────────────────┘          │     • Modo Touchpad (Swipe/Tap)
│                                       │
├───────────────────────────────────────┤
│     (←)           (⌂)          (●)    │ ◄── Voltar | Home | Google Assistente
├───────────────────────────────────────┤
│     (🔇)       [  🔈  │  🔊  ]        │ ◄── Mudo | Volume Down / Volume Up
└───────────────────────────────────────┘
```

### 1. Dois Modos de Navegação Central (Alternáveis no menu `⋮`):
- **Modo D-Pad (Padrão):** Círculo tátil dividido em 4 direções (`Cima`, `Baixo`, `Esquerda`, `Direita`) com botão central `OK / Enter`.
- **Modo Touchpad (Gestos):** Superfície contínua de alta precisão inspirada no touchpad do app oficial do Google TV.
  - **Arrastar o dedo:** Um indicador circular branco suave acompanha o movimento em tempo real.
  - **Flick / Deslizar (Swipe):** Dispara a navegação direcional na TV.
  - **Toque simples (Tap):** Aciona o botão `OK / Enter`.

### 2. Botões de Sistema e Multimídia:
- **Voltar (`AC Back`):** Retorna à tela anterior.
- **Home (`AC Home`):** Vai direto para a tela inicial do Google TV.
- **Google Assistente:** Botão em destaque com fundo branco e o ícone de 4 pontos do Assistente.
- **Power:** Liga / Desliga ou coloca a TV em modo de espera.
- **Áudio:** Botão circular de **Mudo** e pílula dupla com divisor fino para **Volume Down (-)** e **Volume Up (+)**.
- **Feedback Tátil:** Todos os botões emitem vibração háptica curta e responsiva ao toque.

---

## ❓ Perguntas Frequentes e Resolução de Problemas

<details>
<summary><b>1. A TV não encontrou meu celular na busca dela. O que fazer?</b></summary>

O Google TV esconde aparelhos que se identificam como telefone (`PHONE`) na busca dele. Não se preocupe: basta manter a TV na tela *"Adicionar acessório"* e usar o botão **"Buscar TV"** dentro do aplicativo no celular (ou ir em *Configurações > Bluetooth > Parear novo dispositivo* no Android).
</details>

<details>
<summary><b>2. O celular já pareou, mas continua aparecendo como "Desconectado" no app.</b></summary>

1. No aplicativo, toque no topo em *"Desconectado - Toque para conectar"*.
2. Na lista de dispositivos pareados, **toque no seu Chromecast**.
3. Se não conectar em instantes, pegue o controle original da TV e vá em **Configurações > Controles remotos e acessórios**, selecione o seu celular na lista e clique em **"Conectar"**. A TV autorizará a sessão e o app conectará imediatamente.
</details>

<details>
<summary><b>3. O controle funciona se a TV for desligada da tomada?</b></summary>

Sim. O aplicativo armazena o endereço MAC da TV e, toda vez que você abrir o app ou ligar o Bluetooth, ele tenta a reconexão automática ao último aparelho conectado.
</details>

<details>
<summary><b>4. Funciona em outros dispositivos além do Google TV?</b></summary>

Sim! O padrão Bluetooth HID é universal. O controle funciona com:
- Chromecast com Google TV (HD e 4K)
- Smart TVs com Android TV (TCL, Sony, Philips, Philco, etc.)
- TV Boxes (Xiaomi Mi Box, Mi TV Stick, Realmee TV Stick, Fire TV)
- NVIDIA SHIELD TV
- Computadores Windows, macOS e Linux (reconhecido como teclado multimídia Bluetooth)
</details>

---

## 🛠️ Como Compilar o Código-Fonte

Se você é desenvolvedor e deseja modificar ou compilar o projeto:

### Pré-requisitos:
- **JDK 17** (Recomendado: utilize o OpenJDK 17 da pasta `jbr` do Android Studio).
- **Android SDK** com API 34 instalada.

### Passos:
1. Clone o repositório:
   ```bash
   git clone https://github.com/seu-usuario/ControleChromeTVBluetooth.git
   cd ControleChromeTVBluetooth
   ```

2. Compile o APK de depuração:
   - **No Windows (PowerShell):**
     ```powershell
     .\gradlew.bat assembleDebug
     ```
   - **No Linux / macOS:**
     ```bash
     ./gradlew assembleDebug
     ```

3. O APK gerado estará em:
   `app/build/outputs/apk/debug/app-debug.apk`

---

## 📐 Estrutura Técnica do Código

| Pacote | Arquivo | Descrição |
|---|---|---|
| `data` | [`BluetoothHidDescriptor.kt`](app/src/main/java/com/googletv/btremote/data/BluetoothHidDescriptor.kt) | Descritor composto de relatórios HID (Report ID 1 Keyboard + Report ID 2 Consumer Control). |
| `data` | [`PreferencesManager.kt`](app/src/main/java/com/googletv/btremote/data/PreferencesManager.kt) | Gerenciador de persistência do último MAC conectado e preferências de modo. |
| `bluetooth` | [`BluetoothHidController.kt`](app/src/main/java/com/googletv/btremote/bluetooth/BluetoothHidController.kt) | Registrador do perfil HID, gerenciador de conexões, scanner e envio de comandos com delay de 30ms para liberação de tecla. |
| `bluetooth` | [`HidConnectionState.kt`](app/src/main/java/com/googletv/btremote/bluetooth/HidConnectionState.kt) | Modelagem de estados da conexão Bluetooth HID (`Disconnected`, `Connecting`, `Connected`, `Error`). |
| `ui` | [`RemoteViewModel.kt`](app/src/main/java/com/googletv/btremote/ui/RemoteViewModel.kt) | ViewModel intermediando o estado do Compose com as ações de controle. |
| `ui` | [`RemoteScreen.kt`](app/src/main/java/com/googletv/btremote/ui/RemoteScreen.kt) | Tela principal em Jetpack Compose com os botões, TopBar e transição suave entre modos. |
| `ui.components` | [`DPadControl.kt`](app/src/main/java/com/googletv/btremote/ui/components/DPadControl.kt) | Layout circular do direcional com 4 setas e botão central OK. |
| `ui.components` | [`TouchpadControl.kt`](app/src/main/java/com/googletv/btremote/ui/components/TouchpadControl.kt) | Superfície retangular sensível a gestos de swipe e tap com indicador animado. |
| `ui.components` | [`DeviceSelectionSheet.kt`](app/src/main/java/com/googletv/btremote/ui/components/DeviceSelectionSheet.kt) | BottomSheet com lista de dispositivos pareados, scanner de novas TVs e atalhos. |

---

## 📄 Licença

Este projeto está sob a licença [MIT](LICENSE). Sinta-se à vontade para utilizar, modificar e distribuir.
