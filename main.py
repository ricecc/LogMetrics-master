import os
import sys
from pyclustering.cluster.kmedoids import kmedoids
import numpy as np
import pandas as pd
from pyclustering.cluster.silhouette import silhouette
from sklearn import metrics
import csv
import os.path

arg1 = int(sys.argv[1])
arg2 = int(sys.argv[2])
mypath = str(sys.argv[3])

# folder = "C:/Users/Luigi/Desktop/prova 192 senza regoletta/"
folder = mypath + "/"

silhouetteList = []
etichetteList = []
numberOfClusters = []

for file in os.listdir(folder):
    absPath = folder + file
    df1 = pd.read_csv(absPath, delimiter=",")
    df = df1.iloc[:, 1:]  # elimina colonna id
    dm = df.to_records(index=False)
    dm = dm.tolist()

    for i in range(len(dm)):
        for j in range(len(dm[i])):
            if i == j:
                dm[i] = list(dm[i])
                dm[i][j] = '0.0'


    dm = np.array(dm, dtype=object)
    #print da cancellare
    print(dm)
    dm = dm.astype(float)

    dm2 = dm
    vec = []

    for n_clusters in range(arg1, arg2):
        # k = n_clusters
        initial_medoids = np.arange(0, n_clusters)
        initial_medoids = initial_medoids.tolist()

        kmedoids_instance = kmedoids(dm2, initial_medoids, data_type='distance_matrix')
        # Run cluster analysis and obtain results.
        kmedoids_instance = kmedoids_instance.process()

        clusters = kmedoids_instance.get_clusters()
        centers = kmedoids_instance.get_medoids()

        centers = kmedoids_instance.get_medoids()
        lst = [None] * len(dm)
        for i in clusters:
            for j in clusters[clusters.index(i)]:
                lst[j] = clusters.index(i)
                dm2 = dm2.astype(object)
        score = metrics.silhouette_score(dm2, lst, metric="precomputed", random_state=None, dtype=object)
        vec.append(score)

    silhouette_max = max(vec)
    silhouetteList.append(silhouette_max)
    index = vec.index(silhouette_max)
    numero_ottimo_cluster = index + arg1

    initial_medoids = np.arange(0, numero_ottimo_cluster)
    initial_medoids = initial_medoids.tolist()

    # -----------------------------Scelto il numero di cluster ottimale procediamo con la clusterizzazione-------

    kmedoids_instance = kmedoids(dm2, initial_medoids, data_type='distance_matrix')
    # Run cluster analysis and obtain results.
    kmedoids_instance = kmedoids_instance.process()

    clusters = kmedoids_instance.get_clusters()
    numberOfClusters.append(len(clusters))
    centers = kmedoids_instance.get_medoids()

    lst = [None] * len(dm)
    for i in clusters:
        for j in clusters[clusters.index(i)]:
            lst[j] = clusters.index(i)

    # Labels=np.asarray(df1[0])
    labels = df1.columns
    labels = np.asarray(labels)
    labels = labels[1:]
    # Labels=df1['0'].to_numpy
    list_array = np.asarray(lst)
    Labels_definitive = np.column_stack((labels, lst))
    Etichette = Labels_definitive[Labels_definitive[:, 1].argsort()]
    etichetteList.append(Etichette)

with open("" + str(arg1) + str(arg2) + "clustering.txt", "x") as f:
    for var in range(len(silhouetteList)):
        f.write(str(os.listdir(folder)[var]) + '\n')
        f.write(str(silhouetteList[var]) + '\n')
        for elem in etichetteList[var]:
            f.write(str(elem) + '\n')

with open("" + str(arg1) + str(arg2) + "smallOut.csv", "x") as f:
    for var in range(len(silhouetteList)):
        f.write(str(os.listdir(folder)[var]) + "," + str(silhouetteList[var]) + "," + str(numberOfClusters[var]) + '\n')
