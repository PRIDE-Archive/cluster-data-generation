# The Cluster Data Generation Project

This project provides a framework to reanalyse public Proteomics data,
including amount other [PRIDE Data](www.ebi.ac.uk/pride) or [PeptideAtlas](www.peptideatlas.org) . Amount other tools
the framework provides methods for prediction of _Search Parameters_ of public submissions, reanalysis pipelines for
peptide/protein identification, de novo search or Quality assesment of the final results.

### Contact Us:

Please you can contact using github issues: https://github.com/PRIDE-Cluster/cluster-data-generation/issues or to the following email: [Yasset Perez-Riverol](yperez@ebi.ac.uk)

Contributors: [Marc Vaudel](https://github.com/mvaudel) , [Kenneth Verheggen](https://github.com/kverhegg)

### Build the Project

In order to build the project the developer should first clone the project and the corresponding submodules:

```bash
git clone --recursive  https://github.com/PRIDE-Cluster/cluster-data-generation
```

When the porject is download, the developer should make `cd` into the project folder and execute:

```bash
 $ mvn clean
 $ mvn install
 ```

All the tools, and corresponding scripts would be store in the `resources` folder.

### Dabatase Handling

A set of tools has been developed to enable the user to perform the following tasks:

- Download a Protein database from external Repository (e.g UniProt Proteomes): ```FastaDownloadTool```
- Processing a Fasta File including the following tasks: ```FastaProcessingTool```
   - Append a Database to the original Database (e.g contaminants database)
   - Add Decoys to the result database

### Parameters Predictors







