# Social Media Downloader (Android)

Um aplicativo Android nativo e moderno, capaz de baixar vídeos do **TikTok, Instagram e X (Twitter)** sem marca d'água.  
Este projeto demonstra habilidades avançadas em desenvolvimento Android, com foco em **Arquitetura Android Moderna**, **Jetpack Compose** e **UI/UX Design**.

## 🚀 Funcionalidades

- **Suporte Multiplataforma:** Baixe vídeos do TikTok (via TikWM), Instagram e X (via RapidAPI).
- **Downloads sem marca d'água:** Obtém e salva arquivos de vídeo limpos diretamente no dispositivo.
- **UI/UX Moderna:** Interface customizada com **Glassmorphism**, efeitos de brilho ambiente, uma elegante paleta de cores (off-black/off-white) e badges visuais por plataforma.
- **Campo de Entrada Aprimorado:** Campo inteligente com botões de colar/limpar, validação e estados de botão (desativado/carregando).
- **Integração Inteligente com a Área de Transferência:** Detecta automaticamente links copiados das redes sociais suportadas.
- **Feedback Visual:** Transições animadas, indicadores de carregamento e cards de resultados modernos com botões de ação intuitivos ("Salvar", "Compartilhar").

## 📸 Demonstração

<p align="center">
  <img src="img/preview.png" alt="Preview" width="350">
</p>

## 🛠 Stack Tecnológica & Arquitetura

Este projeto segue práticas modernas de desenvolvimento Android, garantindo a separação de responsabilidades e um fluxo de dados reativo.

- **Linguagem:** Kotlin (100%)
- **Framework de UI:** Jetpack Compose (Material Design 3)
- **Arquitetura:** Fluxo de Dados Unidirecional (UDF)
- **Processamento Assíncrono:** Coroutines & Flow
- **Networking:** Retrofit 2 + Gson / Abordagem de API Híbrida (TikWM & RapidAPI)
- **Carregamento de Imagens:** Coil
- **Integração com o Sistema:** Android DownloadManager API

## 📂 Estrutura do Projeto

```
com.naicolasdev.tiktokdownloader
├── ui
│   ├── components   # Componentes reutilizáveis de UI
│   ├── home         # Tela principal e cards de resultado
│   └── theme        # Sistema de design customizado (Cores, Tipografia, Shapes)
├── data
│   └── api          # Integrações de API (TikWM, RapidAPI, Twitter)
├── util
│   └── parser       # Parsers de URLs das redes sociais
└── MainActivity.kt  # Ponto de entrada da aplicação
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
Ele atua como um cliente para APIs públicas disponíveis e **não possui qualquer afiliação, endosso ou vínculo** com o TikTok, Instagram, X ou suas empresas controladoras.

Respeite os direitos de propriedade intelectual dos criadores de conteúdo e utilize esta ferramenta de forma responsável.

---

Desenvolvido por **Nicolas Viana Alves**
