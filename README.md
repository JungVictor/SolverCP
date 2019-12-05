# Solveur PPC
Le but est d'implémenter un solveur de programmation par contraintes qui soit fonctionnel. Est-il optimisé ? Non. Mais il est facile à utiliser.  
La première partie va porter sur comment utiliser le solveur, et la deuxième partie sur l'implémentation de celui-ci. Je vais parler très brièvement des choix que j'ai fait et pourquoi, ainsi que des difficultés rencontrées (pour plus de détails, voir **directement dans le code**. Il est commenté, un peu.) 

## I. Modélisation d'un problème
### 1. Créer un modèle
Pour créer un modèle, on passer par la classe Model. Elle permet de centraliser toutes les informations concernants les variables, les contraintes, etc... et permet de facilement créer les différents élements.  
`Model model = new Model();`

### 2. Ajouter des variables au modèle
Il existe plusieurs manières d'ajouter des variables au modèle. Tout d'abord, on peut les ajouter une par une. Soit on défini le tableau des valeurs possibles, soit on défini la borne inférieur et la borne supérieur du domaine.
  
 _Création d'une variable dont le domaine est {1, 2, 3}_  
`Variable variable = model.addVariable(new int[]{1, 2, 3});`  
`Variable variable = model.addVariable(1, 3);`

Ces deux syntaxes sont équivalentes. Il est préférable d'utiliser la deuxième dans le cas d'un domaine continu, tandis qu'il sera seulement possible d'utiliser la première dans le cas d'un domaine discontinu.  
On peut également décider de créer un tableau de variables. Il s'agit de la même construction que pour la création d'une seule variable, en rajoutant évidemment combien de variables il faut créer.

_Création de n variables dont le domaine est {1, 2, 3}_  
`Variable[] variables = model.addVariables(n, new int[]{1, 2, 3});`  
`Variable[] variables = model.addVariables(n, 1, 3);`

### 3. Définir les contraintes
Pour définir une contrainte, il suffit d'exprimer cette contrainte sous la forme d'une équation mathématique, et d'appeler la fonction `model.addConstraint()` comme suit :

_Contrainte simple où variable[0] < variable[1]_  
`model.addConstraint("x < y", variable[0], variable[1]);`

L'association symbole -> variable se fait automatiquement. Pour plus de précision, voir la partie sur la classe Expression.  
Quelques exemples un peu plus foufous :

`model.addConstraint("(| x - y | + 5) % 5", var[0], var[1]);  `
`model.addConstraint("x^2 + y + z * (5 * (a + b))", v[0], v[1], v[2], v[3], v[4]);`
`model.addConstraint("x = 0", variables[0]);`

### 4. Définir les variables de décisions
Pour avoir plus de contrôle sur le modèle, on défini manuellement quelles sont les variables pour lesquelles on doit effectuer un choix. Cela permet de rajouter plusieurs variables dépendantes d'autres variables qui vont servir dans les contraintes sans augmenter le parcours de l'arbre. Je n'ai pas eu le temps de faire un système optimisé automatique.  

`model.decisionVariables(variables); // ajoute un tableau de variables`  
`model.decisionVariable(variable); // ajoute une variable`  

### 5. Définir une fonction objectif
Pour l'instant, la fonction objectif n'est pas encore pleinement implémentée. Il est possible de poser des objectifs simples, mais pas encore des objectifs aussi complexes que l'on veut.  
Il est possible de définir une fonction objectif à l'aide d'une expression. En plus des expressions classiques, il est possible de définir une somme en fonction d'un pattern. La fonction objectif s'effectue sur des variables que l'on passe en paramètre (donc pas forcément sur toutes les variables du domaines).

`model.minimize("val", variable[n-1]); // minimise la dernière variable`  
`model.maximize("v1 + v2", variable[0], variable[1]); // maximise la somme des deux premières variables`  
`model.minimize(ExpressionBuilder.sum("var * var", N), variables); // minimise la somme du tableau de variables (de taille N)`  

Pour plus d'information sur ExpressionBuilder, voir la section sur les Expressions.  

### 6. Paramètres de résolution et métriques
#### Filtrage
Définir le type de filtrage : `model.setFilter(Constraint.AC3)` ou `model.setFilter("AC3")`.  
Les types de filtrage disponibles sont AC3, AC4, AC6 et AC2001.  

#### Recherche de solutions
On peut définir le nombre de solutions que l'on désire avoir.  
`model.lookingForSolution(n)` pour recherche n solutions. Si n <= 0, alors on recherche toutes les solutions.  

_Attention : Malgré la présence d'une fonction objectif, rechercher n solutions retournera les n premières solutions trouvées, et pas les n solutions optimales._  

#### Lancer une résolution et afficher les "statistiques" du solveur
Lancer la résolution : `model.solve();`  
Récupérer les statistiques : `String stats = model.stats();`  
Récupérer les solutions : `int[][] solutions = model.solutions();`  
Récupérer la meilleure solution (fonction objectif) : `int[] best = model.best();`

Exemple d'affichage _(GolombRuler n = 7, ub = 30, objectif : minimiser la dernière valeur)_ :   
`Building (s) : 0.283`  
`Execution (s) : 3.473`  
`# Fails : 5730`  
`# Backtracks : 1514`  
`# Solutions : 429 (all solutions)`  
`CONSTRAINT : AC2001`  
`OPTIMAL SOLUTION : 0 2 7 13 21 22 25 `

## II. Implémentation