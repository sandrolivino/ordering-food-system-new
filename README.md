<h1>Estrutura do curso</h1>

Curso disponível em: https://www.udemy.com/course/microservices-clean-architecture-ddd-saga-outbox-kafka-kubernetes/learn/lecture/31152934#content


O que será ensinado...


### Microserviços (powered by Spring boot)

### Clean & Hexagonal Architectures

Ambas buscam isolar a camada lógica de domínio de dependências externas.

## Hexagonal Architecture
A arquitetura hexagonal também conhecida como ports and adapters (portas e adaptadores), foi inventada por Alistair Cockburn em 2005. Basicamente, ele se baseia na definição de interfaces bem definidas, que são chamadas de portas.
E implementando as interfaces com adaptadores substituíveis.



## Clean Architecture
Já a arquitetura limpa proposta por Robert Martin em 2012 abrange uma arquitetura limpa em detalhes, incluindo os conceitos de arquitetura hexagonal e arquitetura onion, mas também adicionando a definição de detalhes na implementação da camada de domínio.
Arquitetura limpa usa o princípio de inversão de dependência para seguir estritamente a regra de dependência que só permite e o anel externo apontar para o anel interno e nunca o contrário.


### DDD - Design orientado a domínio.
Em cada microsserviço ao implementar a lógica de negócios, você aplicará os princípios de design orientado a domínio.
Ele basicamente fornece alguns padrões estratégicos e táticos que ajudam você a implementar sua lógica de domínio de uma forma mais fácil de entender, desenvolver e manter.

### Kafka - para os padrões como Saga e CQRS
Quando você segue a abordagem orientada a eventos você precisará usar um barramento de mensagens para usar na notificação de eventos. Usarei o Kafka para essa finalidade, que é resiliente, pois mantém os dados no armazenamento em disco de persistência, e é escalável graças à estratégia de particionamento interno.

Kafka tem o conceito de tópicos para armazenar os dados, e fornece aos produtores a publicação dos dados sobre os tópicos Kafka, assim como os consumidores para ler os dados dos Tópicos Kafka.


Usarei o Kafka na implementação dos padrões Saga, Outbox e CQRS.

## Saga.
Inventado em uma publicação em 1987, Saga é usado para transações de longa duração.

Definição: "O padrão de design saga é uma maneira de gerenciar a consistência de dados entre microsserviços em cenários de transação distribuída. Uma saga é uma sequência de transações que atualiza cada serviço e publica uma mensagem ou evento para disparar a próxima etapa de transação."

A ideia por trás do Saga é criar uma cadeia de transações ACID locais para finalizar uma transação de longa duração em todos os serviços.
Por exemplo, como você verá na próxima aula, neste curso, você terá um aplicativo de pedido de comida que tem serviço de pedidos, serviço de pagamento e o serviço de restaurante.

Para completar um pedido de comida o pedido vai precisar de um pagamento bem sucedido, bem como uma aprovação do restaurante.

_Orquestração com Saga_ - Há também uma abordagem de orquestração para implementar o Saga Pattern que usa um orquestrador para conduzir o saga. Vamos usar coreografia por meio de eventos.
Então vou mostrar usando uma Saga baseada em coreografia, baseada em eventos usando Kafka, sem framework adicional (como Eventuate ou Axon).


## Outbox
Conforme mencionado no padrão Saga, quando você tem vários serviços envolvidos em uma transação de longa duração, como você vai conseguir consistência?

Pense assim, você tem dois serviços, então você tem duas transações locais para realizar uma Saga.
- O primeiro serviço faz alguma transação ACID local e publica um evento para notificar o segundo serviço.
- O segundo serviço escuta o evento e faz sua transação ACID local e publica outro evento.
  Então primeiro serviço ouve o evento do segundo serviço e completa a Saga.

Qual é o problema neste cenário?
Veja, existem duas transações ACID locais, ok... mas e os Eventos publicando? Como garantir que a transação local e a publicação da operação do evento funcionem de maneira consistente?
- Se você se comprometer primeiro com uma transação de banco de dados e depois publicar o evento, e a operação de publicação falha, a Saga não pode continuar e você deixará o sistema em estado inconsistente.
- Em vez disso, se você publicar o evento primeiro e depois tentar confirmar a operação no banco de dados, as coisas podem até piorar, pois a transação do banco de dados local pode falhar, e, nesse caso, você já teria publicado um evento errado, que nunca deveria ter publicado.

Para resolver este problema existem duas abordagens:
1 - Usar o fornecimento direto de eventos, o que significa não usar um banco de dados local e publicar diretamente um evento que será ouvido pelo segundo serviço para dar continuidade à operação da Saga. No entanto, na maioria dos casos, você precisará usar uma transação ACID local, especialmente se estiver tratando de operações monetárias.

2 - OutbBox: neste padrão você não publica os eventos diretamente. Em vez disso, você mantém seus eventos em uma tabela de banco de dados local, chamada Outbox Table.
Observe que esta tabela pertence ao mesmo banco de dados que você usa para operações de banco de dados local.
Nesse caso, portanto, você pode usar uma única transação ACID para concluir suas operações de banco de dados: uma no serviço e mais uma para inserção na tabela de saída. Então você terá uma consistência forte até este ponto.

Depois disso, você concluirá o padrão Outbox lendo os dados da Outbox Table, publicando os eventos.

Para isso, existem duas abordagens:
A) puxando os dados da tabela; ou
B) usando CDC: change data capture, que escutará os logs de transações da Outbox Table.

Em ambos os casos, você deve se preocupar com a implementação para ter certeza de que não irá perder os eventos na Outbox Table e de que eles serão publicados com segurança no barramento de mensagens.

Usarei a abordagem A, de puxar a tabela da Outbox Table e lidarei com possíveis cenários de falha na implementação.


## CQRS (Command Query Responsability Segregation).
Tradução (não exata) - Segregação de Responsabilidade de comando de consulta.
Com o CQRS, você pode projetar partes de leitura e gravação de seu sistema separadamente e permitir a leitura de seu dados de forma mais eficaz.

Isso ocorre porque você pode querer usar diferentes fontes de armazenamentos de dados e gravar por partes.

Por exemplo, digamos que você use um banco de dados relacional para seu banco de dados de gravação e envie seus dados de forma assíncrona para o Elasticsearch.
Em seguida, você pode usar este Elasticsearch para consultar seus dados com mais eficiência.

Além disso, permitirá dimensionar seus sistemas de leitura e gravação separadamente, como resultado natural de um sistema distribuído.

Novamente, esse padrão terminará com uma consistência eventual, porque depois de gravar seus dados no banco de dados de escrita, haverá algum atraso até que seus dados terminem no banco de dados de leitura.

Observe que o mesmo padrão Outbox deve ser usado em conjunto com o padrão CQRS também para que você possa ter certeza de que suas transações locais e operação de publicação de eventos sejam executadas de maneira consistente.


### Kubernetes e Docker.
Kubernetes é um sistema de orquestração de contêineres que automatiza a implantação, dimensionamento e gerenciamento de aplicativos nativos da nuvem. Ele permite executar contêineres docker enquanto reduz as complexidades operacionais.

Você aprenderá a conteinerizar seus serviços e primeiro executá-los localmente usando o Docker. Em seguida, você criará um cluster local do Kubernetes e colocará seus contêineres do Docker nesse cluster local.

Basicamente, você criará implantações e serviços para cada microsserviço e os executará dentro do Kubernetes.

Kafka no Kubernetes
No Kubernetes, você usará CP HELM CHARTS. E para os microsserviços, você criará imagens do Docker localmente.
https://github.com/confluentinc/cp-helm-charts


### Google Cloud e Google Kubernetes Engines
Você implantará seu aplicativo no Google Kubernetes Engine (GKE) e aprenderá a usar o  gcloud command line interface e como enviar imagens de contêiner locais para o Google Container Registry.

Por fim, você executará e dimensionará seus serviços, que desenvolveu neste curso, no Google Kubernetes Engine.

ATENÇÃO: Além disso, lembre-se de baixar o código-fonte das aulas 61, 66 e 96 antes de iniciar mudanças importantes.

Você pode usar o código-fonte como referência ou pular algumas partes que você já conhece ou aprendeu anteriormente no curso. Como as implementações do segundo e terceiro microsserviços são semelhantes ao primeiro





<h2>Arquitetura do Projeto</h2>
Vou criar quatro microsserviços que se comunicam por meio de eventos usando um barramento de mensagens, que será o Kafka.

![](C:\Sistemas\food-ordering-system\food-ordering-system\project-design\project-overview-section.png)

### Order Service

Vamos começar com o serviço de pedidos (Order Service). O serviço de pedidos será o primeiro ponto de contato para os clientes, por isso terá uma API REST que pode ser chamado por uma interface de usuário ou de qualquer cliente Rest (Postman / Imsonia).

Então aqui em Order temos o cliente Http, que será o mensageiro, ou seja, que irá iniciar um pedido.
Vou enviar uma solicitação HTTP para o serviço de pedidos para criar um pedido.
O corpo da solicitação estará no formato json.
Os dados de entrada serão: ID do cliente, ID do restaurante, endereço, Preço e Itens do Pedido (que possuem os detalhes do produto).

Como resposta, o serviço de pedidos retornará o ID de rastreamento e o Status do Pedido. Um pedido será criado no estado Pendente.

Mais tarde, os clientes poderão consultar o serviço de pedidos com um endpoint Http Get e obter o status mais recente, que pode ser Pago, Aprovado, Cancelado etc.

Esse microserviço (Order Service) terá seu próprio Database Local, que no nosso caso será o Postgres.

Além disso, terei um componente de mensagens que nos permitirá comunicar com o barramento de mensagens, que será Kafka.

Este componente de mensagens publicará os eventos que serão criados a partir da camada de domínio do serviço de pedidos.

Portanto, o último mas não menos importante componente será o de lógica de domínio que terá a lógica de negócios do serviço de pedidos.

## Outros serviços

### Payment Service
Neste serviço, terei camada de acesso a dados para me comunicar com banco de dados local.
Em seguida, ele terá o componente de mensagens para se comunicar com o Kafka. E por fim, terá a lógica de negócio para realizar operações de pagamento.


### Restaurant Service
Este serviço aprovará um pedido verificando alguma lógica de negócios como a disponibilidade de um produto. Ele terá, assim como os demais, o acesso aos dados, mensagens e lógica de negócios.


### Customer Service
No início do curso, usarei uma Materialized View no banco de dados do cliente que será preenchida com uma trigger na tabela de cliente, e deixarei o serviço de pedidos consultar os dados do cliente diretamente do esquema do cliente.

Isso será possível, pois usarei a mesma instância de banco de dados, embora os esquemas sejam diferentes para cada microsserviço seguindo um banco de dados por serviço padrão de microsserviços.

ATENÇÃO: mais tarde no curso, mudarei essa estrutura para qual Fonte de eventos e CQRS usando Kafka e mova os dados do cliente para uma tabela no banco de dados de pedidos, em seguida, o Order Service lerá os dados dessa tabela.

Então, por enquanto, vou desenhar o atendimento ao cliente aqui com um banco de dados local e relacioná-lo com o serviço de pedidos (conferir no desenho).

## Kafka: Data flow do serviço de Pedidos.
Os serviços produzirão e consumirão eventos para se comunicarem entre si e eles usarão Kafka para enviar e receber esses eventos.

Em primeiro lugar, para concluir um processo de pedido, a primeira comunicação deve ser entre o serviço de pedidos (Order Service) e o serviço de pagamento (Payment Service). Então devem ser criados dois tópicos Kafka entre eles:
- payment-request-topic
- payment-response-topic

Passos para essa transação:
1) Criar um pedido (Order) em Order Database;
2) Em seguida, ele enviará os eventos criados no pedido para o tópico de solicitação de pagamento.
3) Então o serviço de pagamento vai ouvir este tópico "payment-request-topic".
4) O serviço de pagamento processará os pagamentos no banco de dados de pagamentos - Payment Database.
5) Em seguida, ele criará um evento de pagamento concluído no tópico de resposta de pagamento.
6) Order service irá ouvir o evento produzido por Payment Service em "payment-response-topic".
7) Por fim, o serviço de pedidos consumirá o tópico de resposta de pagamento e atualizará o status do pedido como pago em Order Database.
   Observação: veja que o database local Order Database está sendo atualizado duas vezes, nos passos 1 e 7.
   Para obter aprovação do restaurante, precisamos de mais dois tópicos Kafka.
- restaurant-approval-request
- restaurant-approval-response

8) O serviço de pedidos solicitará uma aprovação ao serviço de restaurante, criando um Order Event com status pago, no tópico de solicitação de aprovação do restaurante "restaurant-approval-request".
9) Em seguida, o serviço de restaurante consumirá esse tópico com a etapa nove.
10) E processar a aprovação do pedido no banco de dados do restaurante Restaurant Database.
11) Restaurant Service produzirá um Approval Event, no tópico de resposta à aprovação do restaurante.
12) Finalmente, o serviço de pedidos consumirá o tópico Kafka de resposta de aprovação do restaurante "restaurant-approval-response" e finalizará o pedido.


Uma coisa a notar é que o Serviço de Pedidos atua como um coordenador para o fluxo SAGA. Porque ele inicia o SAGA, então recebe o evento de pagamento concluído, pede aprovação para o serviço de restaurante e finalmente conclui o pedido com o estado Aprovado.

Nesse ponto, ele retornará o estado Aprovado para os clientes e será consultado usando um Get endpoint.

E depois disso, o cliente pode acionar o próximo passo, como o processo de entrega.

Então eu vou seguir esse fluxo, e, adicionalmente, adicionar a interface Saga Step com métodos de processo e rollback que serão implementados em cada passo.

Cada etapa do SAGA exigirá dois métodos de processamento e reversão "process() e rollback()". Porque em caso de falha em qualquer ponto, as operações anteriores precisam ser compensadas no padrão SAGA.

Portanto, isso será alcançado usando o método de rollback em cada etapa da SAGA. E mais tarde quando implementamos o padrão Outbox junto com SAGA, também rastrearemos o status SAGA e o status da OutbBox ao lado do status do pedido.

==== Até aqui pressupõe-se que o fluxo está completo, mas precisamos fazer melhorias, implementando todos os Patterns.

### Melhorias no Projeto

## Implementar os princípios de Hexagonal (Clean) Architecture
Basicamente, com essa arquitetura, você isolará sua lógica de domínio da infraestrutura e das dependências externas.

Isso será alcançado separando o serviço em diferentes unidades implantáveis. Como se pode ver na imagem, aqui tenho caixas diferentes para cada um dos componentes e isso implica que terei módulos diferentes para cada uma delas.

Portanto, cada uma das camadas, acesso a dados, mensagens, Rest API e lógica de negócios deve ser implantável separadamente e isso requer ter diferentes unidades implantáveis como componentes, como você verá na seção de implementação.

## 1 - Definir portas de entrada e saída dos microserviços
Observe que todas as portas de entrada e saída, são definidas no módulo de domínio. Então aqui vou desenhar portas de entrada e portas de saída para o serviço de pedidos (Order Service).

Essas portas nada mais são do que interfaces que precisam ser implementadas por adaptadores. Então vou ter alguns adaptadores nessa arquitetura.

## Adaptadores + portas de entrada e saída dos microserviços
O primeiro tipo de adaptador é chamado de adaptador primário, que é o cliente HTTP (na figura), e ele chamará a lógica do domínio.

A lógica do domínio implementará as portas de entrada e essas portas de entrada serão usadas pela Rest API (presente em Order Service).

Deste modo, vamos pensar no fluxo de dados: vou chamar o Order Service pela HTTP cliente, no caso, o Postman (inicialmente, enquanto não tem frontEnd) e ele primeiro atingirá o componente Rest API que terá uma classe de controladora.

Em seguida, ele usará a porta de entrada definida na lógica do domínio e chamará os métodos expostos por esta porta de entrada. Para usar a porta de entrada, o módulo Rest API terá uma dependência do módulo de domínio (Inversão de dependência). Desta forma, ela será o nosso adaptador secundário.

Os componentes de acesso a dados e mensagens terão esses adaptadores secundários.

Agora, se continuarmos o fluxo de dados após receber as requisições do cliente e chamar os métodos da camada de domínio, processaremos a lógica de negócios e depois usaremos as portas de saída para chamar o acesso aos dados e as camadas de mensagens da camada de domínio.


## 2 - DDD Domain Driven Design
A segunda melhoria neste projeto será a aplicação dos princípios DDD - design orientados ao domínio.

Portanto, na lógica de domínio, aplicarei padrões de design orientados por domínio estratégico e tático:
- Aggregates: é um grupo de objetos de negócios que sempre precisam ser consistentes.
- Entities: objetos de negócio
- VOs (value objects): são usados para definir objetos simples e imutáveis com nomes controlados pelo domínio. Um exemplo é ter um objeto de dinheiro em vez de manter um tipo Java Big Decimal diretamente.
- Domain Services: são usados para lidar com a lógica de negócios que abrange várias rotas agregadas e as lógicas que não se enquadra em nenhuma entidade por natureza.
- Application Services: é o ponto de contato com o exterior do domínio. Portanto, qualquer outra camada que queira se comunicar com a camada de domínio precisa usar o Application Services.
- Domain Event: são usados para enviar notificações para outros serviços que são executados em diferentes contextos.


## 3 - SAGA Pattern: process & rollback (compensating transactions)
Atuará sobre Domain Event essencialmente.

## 4 - OutbBox Pattern: Pulling OutbBox Table with scheduler
Precisamos combinar Saga com o padrão Outbox para obter uma solução consistente. Estarei usando Pulling OutbBox Table com um agendador, mantendo o estado de Saga, Outbox Table e Order OutbBox para cada micro serviço.

Com o padrão OutbBox, adicionarei uma OutbBox Table de saída local no mesmo banco de dados que o microsserviço persiste seus dados (banco local do microserviço).

Então, em vez de publicar um evento diretamente, primeiro salvarei os eventos nesta tabela OutbBox Table. Usarei a mesma transação ACID que o serviço de pedidos usa para as operações do banco de dados local. Assim, terei certeza de que o evento será criado automaticamente dentro do banco de dados local usando a transação ACID.

O próximo passo será ler os eventos da tabela de saída e enviá-los para o barramento de mensagens. Para isso, usarei novamente Pulling OutbBox Table com um agendador.

A mesma arquitetura se aplica aos serviços de pagamento de restaurante. Então terei agendadores em cada serviço que lê os eventos da OutbBox Table.

Além disso, garantirei que o agendador marque os eventos como enviados se e somente se puder enviar os eventos para Kafka.

O padrão OutbBox terá que cobrir cenários de falha, como garantir idempotência usando a tabela OutbBox Table em cada serviço, evitando problemas de simultaneidade com optimists locks e restrições de banco de dados e atualizando a Saga e o status do pedido para cada operação.


## 5 - CQRS Pattern:
Então, depois de completar os padrões Saga e Outbox, vou revisitar o atendimento ao cliente como mencionei e atualizá-lo para usar o padrão CQRS.

Como mencionado, inicialmente, usarei uma Materialized View com um trigger, mas depois irei alterá-la de acordo com a origem de eventos e criar os dados do cliente em uma nova tabela no banco de dados do serviço de pedidos.


## 6 - Kubernetes and GKE
Por fim, vou dockerizar os serviços e executá-los em um cluster local do Kubernetes e, em seguida, implantá-los no mecanismo Google Kubernetes para gerenciar, dimensionar e implantá-los facilmente usando o console do Google e o gcloud command.



## Iniciando o projeto
### 1 - Criar o projeto como maven
### 2 - Deletar a pasta src
### 3 - Incluir no pom.xml 
```
  <packaging>pom</packaging>
```
No módulo principal, não faz sentido que o empacotamento seja do tipo "jar", tendo em vista que não há mais uma pasta src.

### 4 - Acrescentar a dependência para o spring boot.
```
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.0</version>
        <relativePath />
    </parent>
```

Esse caminho relativo "<relativePath />" é usado para pesquisar no sistema de arquivos local o projeto maven pai. Mas como temos um projeto pai externo que é o spring boot, não precisamos pesquisar o filesystem local.
O pom pai será carregado da Biblioteca de inicialização do Spring.
Portanto, definir o caminho relativo como vazio é uma prática recomendada, se seu pai for um projeto externo como o Spring boot.

### 5 - Adicionar as seções de gerenciamento de dependências, de dependências e a seção de compilação "<build>"
```
    <dependencyManagement></dependencyManagement>
    
    <dependencies></dependencies>
    
    <build></build>
```

Por enquanto, vou adicionar apenas um plugin na seção de "<build>". Adicionaremos outras dependências ao criar os demais serviços.

O plugin que vou adicionar é o plugin do compilador Maven, que irá definir a versão Java para um projeto Maven. Então, vou adicionar a seção de plugins e usar a tag plugin para adicionar o plugin do compilador Maven.
```
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <release>17</release>
                </configuration>
            </plugin>
        </plugins>
    </build>
```

Agora, precisamos remover a tag <maven.compiler.source> e <maven.compiler.target> da tag <properties>, já que já temos o plugin configurado.
```
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>        
```

Vamos passar a versão do plugin para a dentro da tag properties, para ter todas as versões em um só local.
```
    <properties>
        <maven.compiler-plugin.version>3.9.0</maven.compiler-plugin.version>
    </properties>

    <version>${maven.compiler-plugin.version}</version>
```

### 6 - Confirmar se o projeto está usando Java 17 e se o maven está corretamente instalado
JAVA: File >> Project Structure

MAVEN: No terminal -> mvn -version


<h2>Definir os pacotes básicos do módulo order-service</h2>

![](C:\Sistemas\food-ordering-system\food-ordering-system\project-design\order-service-hexagonal-section-2.png)

* order-service (sem src): container do serviço
    * order-domain (sem src): container do domínio
      * order-domain-core
    * order-application: endpoint para o frontEnd
    * order-dataaccess: operações de dados
    * order-messaging: comunicação com os demais serviços
    * order-container: container dos módulos de "order-service"

Temos quatro módulos. Então, como mencionado, a ideia é desenvolver o domínio separadamente e fazer plugins de outros módulos para o módulo de domínio.

Para isso, preciso de um módulo que crie o arquivo jar executável para o microsserviço, e tenha uma dependência para todos os outros módulos.

Chamamos este módulo de "order-container".

Como ele conterá todos os outros módulos, e, a partir dele será criado um arquivo jar executável, criarei uma imagem do Docker e a partir disso executável jar e que poderá ser executado em um container docker.


* Com os módulos criados, remover a propriedade do compilador maven gerado automaticamente de todos os arquivos pom.

Ele é gerado pelo intellij, mas já configurei o plugin do compilador Maven com JDK 17 no arquivo pom.xml base.

Observar que o módulo order-service, contém 05 sub-módulos:

```
    <modules>
        <module>order-application</module>
        <module>order-container</module>
        <module>order-dataaccess</module>
        <module>order-domain</module>
        <module>order-messaging</module>
    </modules>
```

No entanto, em order-domain, quero criar mais dois sub-módulos, para separar o "core" do domínio dos serviços de aplicativos, que são os serviços que expõem métodos de domínio para o exterior.
- order-domain-core: conterá entidades, objetos de valor e serviços de domínio.
- order-application-service: 

ATENÇÃO: Lembrar de remover a versão do maven do pom desses sub-módulos e remover a pasta src de order-domain.

<h2>Estabelecendo a dependência entre os módulos</h2>
Iniciando pelo módulo order-domain-core. Nada foi colocado de dependência, pois esse módulo deve ser o mais independente possível

Já o order-application-service é dependente de order-domain-core.

Observação: a versão (<version>1.0-SNAPSHOT</version>) da dependência não vai ser colocada no pom de order-application-service, será colocado no pom da aplicação (food-ordering-system)
```
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.food.ordering.system</groupId>
            <artifactId>order-domain-core</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

O mesmo deve ser feito com todos os demais sub-módulos, exceto order-container, pois não será utilizado pelos outros módulos. Ele simplesmente terá uma dependência de todos os módulos para criar um único arquivo jar executável e executá-lo como um arquivo jar.
