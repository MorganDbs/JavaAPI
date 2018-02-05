# JavaAPI

# Contributors
 - Matmat Myriam
 - Dubois Morgan
 https://github.com/MorganDbs/JavaAPI
# Template request

``` bash
# Request (PUT)
/commandes/{id}/livraison?token={token}

# Description
Change les informations de livraison d'une commande à partir du JSON fourni

# Body
"livraison":{
		"date": "15-2-2018",
		"heure": "4:45"
}
```

``` bash
# Request (PUT)
/commandes/{id}/payer?token={token}

# Description
Change l'état d'une commande à 'payer'

# Body
{
	"numero": "4641 1517 4230 3736",
	"date": "05-19",
	"cvv": "445"
}
```

``` bash
# Request (PUT)
/commandes/{id}/etat?token={token}

# Description
Change l'état d'une commande en fonction du JSON fourni

# Body
{
	"etat": "attente"
}
```

``` bash
# Request (POST)
/commandes?token={token}

# Description
Crée une commande à partir du JSON fourni

# Body
{
	"nom_client": "qsd",
	"prenom_client": "jl",
	"mail_client": "qq@mail.fr",
	"livraison":{
		"date": "20-3-2019",
		"heure": "23:59"
  }
}
```

``` bash
# Request (POST)
/commandes/{id}/sandwichs?token={token}

# Description
Ajoute un sandwich à une commande à partir du JSON fourni

# Body
{
	"sandwich": "ab-fuga",
	"quantity": "4",
	"taille": "complet"
}
```

``` bash
# Request (PUT)
/commandes/{id}/sandwichs?token={token}

# Descrption
Modifie un sandwich d'une commande à partir du JSON fourni

# Body
{
	"id": "834f81fb-2234-4118-ac8f-183704b981ef",
	"quantity": "8",
	"taille": "complet"
}
```

``` bash
# Request (DELETE)
/commandes/{id}/sandwichs?token={token}

# Description
Supprime le sandwich d'une commande dont l'ID est fourni et le JSON est fourni

# Body
{
	"sandwich": "ab-fuga",
	"taille": "complet"
}
```
