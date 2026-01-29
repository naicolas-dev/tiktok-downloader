# TikTok Video Downloader (Android)

Um aplicativo Android nativo e moderno, capaz de baixar vídeos do TikTok **sem marca d’água**.  
Este projeto demonstra habilidades avançadas em desenvolvimento Android, com foco em **Arquitetura Android Moderna**, **Jetpack Compose** e **UI/UX Design**.

## 🚀 Funcionalidades

- **Downloads sem marca d’água:** Obtém e salva arquivos de vídeo MP4 limpos diretamente no dispositivo.
- **UI/UX Moderna:** Interface customizada com **Glassmorphism**, efeitos de brilho ambiente e gradientes inspirados no TikTok.
- **Gerenciamento de Estado Reativo:** Atualizações de UI em tempo real utilizando `StateFlow` e estados genéricos (Loading, Success, Error).
- **Integração Inteligente com a Área de Transferência:** Detecta automaticamente links do TikTok copiados.
- **Feedback Visual:** Transições animadas e indicadores de carregamento.

## 📸 Demonstração

<img src="img\preview.jpeg" alt="Preview" width="600">

## 🛠 Stack Tecnológica & Arquitetura

Este projeto segue a arquitetura recomendada **MVVM (Model-View-ViewModel)**, garantindo separação de responsabilidades e facilidade de testes.

- **Linguagem:** Kotlin (100%)
- **Framework de UI:** Jetpack Compose (Material Design 3)
- **Arquitetura:** MVVM + Fluxo de Dados Unidirecional (UDF)
- **Processamento Assíncrono:** Coroutines & Flow
- **Networking:** Retrofit 2 + Gson
- **Carregamento de Imagens:** Coil
- **Integração com o Sistema:** Android DownloadManager API

## 📂 Estrutura do Projeto

```
com.naicolasdev.tiktokdownloader
├── ui
│   ├── components   # Componentes reutilizáveis de UI (GlassPanel, GradientButton)
│   ├── home         # Tela principal e cards de resultado
│   └── theme        # Sistema de design customizado (Cores, Tipografia, Shapes)
├── data
│   └── api          # Serviço Retrofit e modelos de dados
├── viewmodel
│   └── MainViewModel.kt  # Lógica de gerenciamento de estado
└── MainActivity.kt       # Ponto de entrada da aplicação
```

## 🔧 Como Executar

1. Clone o repositório:
   ```bash
   git clone https://github.com/naicolas-dev/tiktok-downloader.git
   ```

2. Abra o projeto no Android Studio e aguarde a sincronização do Gradle.

3. Execute o app em um dispositivo físico ou emulador.

## 📝 Licença & Aviso Legal

🔑 **[LICENÇA](LICENSE)**

Este projeto é destinado **exclusivamente para fins educacionais e de portfólio**.  
Ele atua como um cliente para APIs públicas disponíveis e **não possui qualquer afiliação, endosso ou vínculo** com o TikTok ou a ByteDance.

Respeite os direitos de propriedade intelectual dos criadores de conteúdo e utilize esta ferramenta de forma responsável.

---

Desenvolvido por **Nicolas Viana Alves**
