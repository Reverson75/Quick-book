# Quick Book - Gerenciador Inteligente de Tarefas

**Quick Book** não é apenas uma lista de afazeres. É um assistente de produtividade proativo para Android, projetado para gerenciar suas tarefas de forma inteligente, com automações que trabalham por você. Desde lembretes baseados em localização até a criação automática de tarefas a partir de e-mails, o Quick Book foi construído para ser o seu segundo cérebro.

## ✨ Funcionalidades Principais

Este projeto implementa um conjunto robusto de funcionalidades que demonstram as melhores práticas do desenvolvimento Android moderno:

#### Gerenciamento Completo de Tarefas
- **CRUD Completo:** Crie, edite, visualize e delete tarefas com uma interface limpa e intuitiva.
- **Priorização Visual:** A lista de tarefas é ordenada automaticamente por data de entrega e complexidade.
- **Deletar com Segurança:** Deslize para deletar uma tarefa, com um feedback visual e uma opção de "Desfazer" (`Snackbar`) para evitar erros.

#### Lembretes e Automações Inteligentes
- ⏰ **Lembretes por Horário:** Defina uma data e hora para suas tarefas e receba uma notificação no momento exato.
- 📍 **Lembretes por Localização (Geofencing):** Adicione um local a uma tarefa (ex: "Supermercado") e seja notificado assim que você chegar perto do local.
- 📧 **E-mail para Tarefa:** Configure palavras-chave (ex: "pagar", "urgente") e o app irá, periodicamente, verificar sua caixa de entrada e criar tarefas automaticamente a partir dos e-mails que correspondem.
- ✍️ **Criação Inteligente (Smart Input):** Digite "Reunião com a equipe amanhã às 15h" e o aplicativo preencherá os campos de data e hora para você.
- 🔁 **Tarefas Recorrentes:** Configure tarefas para se repetirem diariamente, semanalmente ou mensalmente.
- 😴 **"Soneca Inteligente" (Smart Snooze):** Adie uma notificação com um clique ou marque uma tarefa como concluída diretamente da notificação.

#### Interface e Experiência do Usuário
- 🎨 **Material Design 3:** Interface moderna e limpa, seguindo as últimas diretrizes de design do Google.
- 🌍 **Multi-idioma:** Suporte completo para Inglês e Português, com uma interface de seleção elegante (`BottomSheet`).
- 🔐 **Login Seguro:** Autenticação local com credenciais de e-mail e "Senha de App" armazenadas de forma segura e criptografada no dispositivo.
- ✨ **Ícone Adaptativo:** Ícone profissional que se adapta ao formato do sistema de cada usuário.

## 🛠️ Tech Stack & Arquitetura

O Quick Book foi construído com uma arquitetura moderna e escalável, utilizando as bibliotecas recomendadas pelo Google para o desenvolvimento Android.

- **Linguagem:** **Java**
- **Arquitetura:**
  - **MVVM** (Model-View-ViewModel)
  - **Padrão de Repositório** para abstrair as fontes de dados.
- **Componentes de Arquitetura do Android Jetpack:**
  - **Room:** Para persistência de dados local (banco de dados SQL).
  - **ViewModel:** Para gerenciar os dados da UI de forma consciente ao ciclo de vida.
  - **LiveData:** Para construir observadores de dados que atualizam a UI automaticamente.
  - **Navigation Component:** Para gerenciar a navegação entre as telas (`Fragments`).
  - **WorkManager:** Para agendar tarefas em segundo plano de forma confiável e otimizada (sincronização de e-mail).
- **UI:**
  - **Android Views** com **Material Design 3 Components**.
  - **RecyclerView** para exibição eficiente de listas.
  - **ConstraintLayout** para layouts flexíveis e responsivos.
- **Serviços Externos:**
  - **Google Play Services (Location):** Para os serviços de Geofencing.
  - **Jakarta Mail (anteriormente JavaMail):** Para a conexão com servidores de e-mail via IMAP.
- **Segurança:**
  - **AndroidX Security (`EncryptedSharedPreferences`):** Para armazenar credenciais sensíveis de forma criptografada.

## 🚀 Como Configurar e Rodar

1.  Clone este repositório para sua máquina local.
2.  Abra o projeto no Android Studio.
3.  Aguarde o Gradle sincronizar todas as dependências.
4.  Clique em "Run 'app'".

**Para testar a funcionalidade de Sincronização de E-mail:**

1.  Em sua Conta Google, ative a **Verificação em Duas Etapas**.
2.  Ainda na sua Conta Google, gere uma **"Senha de App"** de 16 caracteres.
3.  Na tela de login do Quick Book, use seu endereço de e-mail e a **Senha de App** gerada (não sua senha normal do Google).

## 👨‍💻 Desenvolvido por

- **Autor:** Reverson
- **Assistência de IA:** Gemini in Android Studio
