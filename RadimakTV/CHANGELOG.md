# Histórico de versões

## 0.7.1 — Mais Brasil

- Adicionado o Servidor 3 com a lista mundial pública do IPTV-org.
- Adicionado o Servidor 4 com uma seleção brasileira de canais abertos.
- Preparada assinatura permanente para atualizações futuras sem reinstalação.

## 0.6.1 — player reforçado

- Nova identidade de instalação para evitar conflitos de assinatura com versões antigas.
- Player com redirecionamentos, tempos de conexão adequados e nova tentativa manual.
- Mensagens específicas para canal fora do ar, bloqueio regional, DRM e codec incompatível.

## 0.6.0 — servidores públicos verificados

- Substitui a origem interna anterior por duas fontes públicas, sem credenciais.
- Adiciona seleção direta entre **Servidor 1** (Brasil) e **Servidor 2** (canais gratuitos internacionais).
- Mantém caches separados para cada servidor, permitindo alternar sem perder o catálogo já carregado.
- Ignora páginas de YouTube e Twitch que não são reproduzíveis pelo player interno.
- Reconhece grupos com o termo “Cine” como canais de filmes.

## 0.5.0 — compilação Android limpa

- Reconstrução integral pelo Android Gradle Plugin e ferramentas oficiais do Android.
- Remove todas as alterações binárias aplicadas aos APKs provisórios anteriores.
- Mantém a origem M3U preconfigurada, a grade de cartões e as abas TV, Filmes e Séries.
- Gera código DEX, recursos, manifesto, alinhamento e assinatura diretamente pela cadeia oficial.

## 0.4.5 — bytecode de inicialização corrigido

- Remove a sequência inválida que podia causar fechamento imediato em alguns aparelhos.
- Preenche integralmente o método interno com instruções de retorno válidas.
- Mantém a lista preconfigurada e a atualização direta sobre a versão 0.4.4.

## 0.4.4 — inicialização corrigida

- Corrige o formato interno da origem preconfigurada para ser reconhecido na inicialização.
- Mantém assinatura v2, alinhamento ZIP e atualização direta sobre a versão 0.4.3.

## 0.4.3 — instalação corrigida

- Assinatura APK v2 para compatibilidade com aparelhos Android recentes.
- Identidade de instalação própria para evitar conflito com versões assinadas anteriormente.
- Origem M3U preconfigurada e carregamento automático mantidos.

## 0.4.2 — lista preconfigurada

- A lista da Radimak TV é configurada automaticamente no primeiro início.
- Instalações anteriores migram para a origem HTTPS fornecida nesta versão.
- A interface preconfigurada não solicita URL ou arquivo M3U.
- O botão de ajustes passa a oferecer apenas a atualização do catálogo.
- A credencial de compilação fica fora do projeto-fonte distribuído.

## 0.4.1 — recuperação de falhas do servidor

- Segunda tentativa automática quando a conexão da lista falha.
- Nova tentativa sem compactação para servidores com respostas GZIP incompletas.
- Itens já recebidos são preservados quando uma lista grande é interrompida.
- Cache privado da última lista carregada para manter os cards durante oscilações futuras.
- Falhas de atualização não apagam mais o catálogo salvo.
- Botão **Tentar novamente** adicionado ao estado de lista vazia.

## 0.4.0 — interface focada na lista M3U

- Navegação inferior simplificada para **TV**, **Filmes** e **Séries**.
- Catálogo redesenhado em grade de cartões quadrados.
- Busca independente em cada seção da lista.
- Configuração, atualização e remoção da lista movidas para o botão de ajustes.
- Filtro do catálogo executado fora da interface para manter a navegação fluida em listas grandes.
- Limite de segurança ampliado para 250 mil itens e 1,25 milhão de linhas.
- Catálogo TMDB, início, busca global e perfil removidos da navegação visível.

## 0.3.1 — compatibilidade com listas grandes

- Leitura contínua de listas M3U, sem o limite anterior de 15 MB em memória.
- Limite ampliado para até 100 mil itens e 500 mil linhas.
- Tempo de resposta ampliado para servidores com catálogos grandes.
- Suporte a respostas compactadas com GZIP.
- Suporte a `User-Agent` e `Referer` anexados à URL do stream.
- Mensagens específicas para credenciais recusadas, servidor indisponível, endereço inválido e tempo esgotado.

## 0.3.0 — listas M3U e TV no app

- Nova aba **TV** para adicionar uma lista M3U por URL ou arquivo.
- Leitura de nomes, grupos, logos, `User-Agent` e `Referer`.
- Classificação automática em TV ao vivo, filmes e séries.
- Busca e filtros locais, com contagem por categoria.
- Reprodução interna com AndroidX Media3/ExoPlayer.
- Compatibilidade com URLs HTTP legadas e recomendação explícita de HTTPS.
- URL mascarada na interface, armazenamento privado e backup do app desativado.
- Nenhuma lista, credencial ou canal incorporado ao projeto.

## 0.2.0 — fontes gratuitas oficiais

- Plex, ViX e Pluto TV adicionados e ativados por padrão.
- Atalhos para abrir o aplicativo ou site oficial de cada fonte gratuita.
- Nova seção **Grátis com anúncios** na tela inicial.
- Descoberta de filmes e séries gratuitos no Brasil pelos dados de provedores do TMDB.
- Disponibilidade diferencia assinatura, grátis e grátis com anúncios.
- Cartões de provedores reconhecidos abrem o player oficial.
- Textos de privacidade, reprodução e DRM atualizados.

## 0.1.0 — primeira versão funcional

- Aplicativo Android nativo com identidade Radimak TV.
- Navegação por Início, Filmes, Séries, Busca e Perfil.
- Catálogo de demonstração para uso imediato.
- Integração configurável com TMDB e disponibilidade JustWatch no Brasil.
- Netflix, Prime Video, Disney+, Max e Globoplay ativados por padrão.
- Paramount+ desativado por padrão.
- Minha Lista local.
- Player Media3/ExoPlayer para arquivos pessoais.
- APK de depuração assinado e pronto para instalação manual.
- Testes unitários e Android Lint executados sem erros.
