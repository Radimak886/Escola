# Radimak TV

Aplicativo Android nativo focado em organizar e reproduzir listas M3U autorizadas.

O APK personalizado inclui duas fontes públicas preconfiguradas. O catálogo é carregado automaticamente e a interface não solicita a inclusão de uma lista.

## O que funciona

- Navegação direta por **TV**, **Filmes** e **Séries**.
- Escolha entre **Servidor 1** (Brasil) e **Servidor 2** (canais gratuitos internacionais).
- Grade de cartões quadrados com logos ou capas fornecidas pela lista.
- Busca local por nome ou grupo dentro de cada seção.
- Configuração por URL HTTP/HTTPS ou arquivo M3U escolhido no Android.
- Classificação automática do catálogo em TV ao vivo, filmes e séries.
- Leitura contínua de listas grandes, com limite de segurança de 250 mil itens.
- Cache privado da última lista carregada para manter o catálogo disponível durante oscilações do servidor.
- Segunda tentativa automática de conexão e reaproveitamento dos itens recebidos quando uma lista grande é interrompida.
- Suporte a grupos, logos, `User-Agent` e `Referer` declarados pela lista.
- Reprodução dentro do app com AndroidX Media3/ExoPlayer quando o formato, codec e autenticação forem compatíveis.
- HLS, DASH e formatos progressivos compatíveis com o aparelho.

## Usar uma lista M3U autorizada

1. Abra o Radimak TV e toque no botão de ajustes no canto superior direito.
2. Cole uma URL M3U ou selecione **Importar arquivo M3U**.
3. Aguarde a leitura e alterne entre **TV**, **Filmes** e **Séries** na barra inferior.
4. Toque em um cartão para abrir o player interno.

A origem e o cache ficam nos dados privados do aplicativo para permitir atualizações posteriores. Eles não são incluídos no código-fonte nem no backup do Android. O botão **Remover** apaga a origem, o cache e os itens carregados deste aparelho.

URLs HTTP são aceitas por compatibilidade, mas trafegam a lista e possíveis credenciais sem criptografia. Use HTTPS sempre que o provedor oferecer.

O app não inclui nem comercializa listas, canais, filmes ou séries. Use apenas fontes públicas ou conteúdo próprio, licenciado ou de domínio público. Links expirados, DRM, codecs incompatíveis e métodos especiais de autenticação podem impedir a reprodução.

## Abrir no Android Studio

1. Instale o Android Studio com Android SDK 35.
2. Abra a pasta `RadimakTV`.
3. Aguarde a sincronização do Gradle.
4. Execute a configuração `app` em um aparelho Android 8.0 ou superior.

Para substituir as fontes preconfiguradas em uma compilação própria, copie `secrets.properties.example` como `secrets.properties` e informe `BUNDLED_M3U_URL_1` e `BUNDLED_M3U_URL_2`. O arquivo real é ignorado pelo Git e não é incluído no pacote do projeto-fonte.

Para gerar um APK de teste:

```bash
./gradlew assembleDebug
```

O resultado será criado em `app/build/outputs/apk/debug/app-debug.apk`.

## Tecnologias

- Kotlin 2.0
- Jetpack Compose e Material 3
- AndroidX Navigation
- AndroidX Media3/ExoPlayer
- Coil
