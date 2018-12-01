package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import Util.IOUtil;

public class LuceneIndexer {
	private ParaSet paraSet;
	public static final String TITLE = "title";
    public static final String ABSTRACT = "abs";
    public static final String ID = "id";
	
	public LuceneIndexer(ParaSet paraSet) {
		this.paraSet = paraSet;
	}
	
	
	public void index(String luceneIndexPath, String catsFilePath,
			String dataRootPath, String seedwordPath) {

		loadFileList(catsFilePath);

        //write index
        startIndex(dataRootPath, luceneIndexPath);
	}
	
	private void loadFileList(String catsFilePath) {
		paraSet.fileList = new ArrayList<FileInfo>();

        BufferedReader br;
        String line;
        String vecs[];
        int cateIndex = 0;
        try {
            br = new BufferedReader(new FileReader(catsFilePath));
            while ((line = br.readLine()) != null) {
            	FileInfo fi = new FileInfo();
                vecs = line.split(" ");
                fi.path = vecs[0];
                String checkStr = fi.path.split("/")[0];
                if(checkStr.equals("test")) {
                	fi.check = true;
                }
                else {
                	fi.check = false;
                }
                for (int i=1; i<vecs.length; i++) {
                	String cate = vecs[i];
                	if (paraSet.cat2IdMap.containsKey(cate)) {
                    	fi.cates.add(paraSet.cat2IdMap.get(cate));
                    }
                    else {
                    	paraSet.cat2IdMap.put(cate, cateIndex);
                    	paraSet.id2CatMap.put(cateIndex, cate);
                    	fi.cates.add(cateIndex);
                    	cateIndex++;
                    }
                }
                paraSet.fileList.add(fi);
            }
            br.close();
        } catch (Exception e) {
            System.out.println("catalog file path is invalid");
            e.printStackTrace();
            System.exit(-1);
        }
	}
	
	private int startIndex(String dataRootPath, String indexPath) {
        int beginId = 0;
        try {
            List<String[]> paperList = loadAbs(dataRootPath, paraSet.fileList);
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            // BufferedReader stopwordsReader = new BufferedReader(new FileReader("D:\\dataSet\\stopwords-1.txt"));
            Analyzer analyzer =
                    // new StandardAnalyzer(stopwordsReader);
                    new StandardAnalyzer();
//          new PorterStemAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            iwc.setOpenMode(OpenMode.CREATE);


            // Optional: for better indexing performance, if you
            // are indexing many documents, increase the RAM
            // buffer.  But if you do this, increase the max heap
            // size to the JVM (eg add -Xmx512m or -Xmx1g):
            //
            // iwc.setRAMBufferSizeMB(256.0);

            IndexWriter writer = new IndexWriter(dir, iwc);

            // make a new, empty document
            Document doc = new Document();
            Field titleField = new StoredField(TITLE, "");
            doc.add(titleField);

            // indexed, not tokenized, and stored.
            Field idField = new StringField(ID, "", Store.YES);
            doc.add(idField);

            FieldType fieldTypeForAbs = new FieldType();
            fieldTypeForAbs.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
            fieldTypeForAbs.setTokenized(true); // we need to tokenize the abstract
            fieldTypeForAbs.setStored(true); // we do not need to store the whole abstract

            // Yes, we always need term vectors for our research work.
            fieldTypeForAbs.setStoreTermVectors(true);
            // Sometimes, we need the token's positions for this field.
            fieldTypeForAbs.setStoreTermVectorPositions(true);
            fieldTypeForAbs.freeze(); // no further change can be made.
            Field absField = new Field(ABSTRACT, "", fieldTypeForAbs);
            doc.add(absField);

            int id = beginId;
            for (String[] record : paperList) {
                idField.setStringValue(String.valueOf(id));
                titleField.setStringValue(
                        record[0] == null ? "null" : record[0]);
                absField.setStringValue(
                        record[1] == null ? "null" : record[1]);
                // cateField.setStringValue(record[2]==null?"null":record[2]);

                if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
                    // New index, so we just add the document (no old document can be there):
                    writer.addDocument(doc);
                } else {
                    // Existing index (an old copy of this document may have been indexed) so
                    // we use updateDocument instead to replace the old one matching the exact
                    // path, if present:
                    // System.out.println("updating " + record[0]);
                    //   writer.updateDocument(
                    //         new Term(ID, String.valueOf(id)), doc);
                    writer.addDocument(doc);

                }

                if (++id % 1000 == 0) {
                    // with this information, we can guess how much time
                    // it cost to index the whole corpus
                    System.out.println(id + " have been indexed...");
                }

            }

            // NOTE: if you want to maximize search performance,
            // you can optionally call forceMerge here.  This can be
            // a terribly costly operation, so generally it's only
            // worth it when your index is relatively static (ie
            // you're done adding documents to it):
            //
            // ****** In our research work, the dataset is almost  *****
            //******* static, and we often index them only once. *****
            //******* So, we need turn this call on. ******
            writer.forceMerge(1);
            writer.close();
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

    }
	
	private static List<String[]> loadAbs(String dataRootPath, List<FileInfo> fileList) throws IOException {
        ArrayList<String[]> list = new ArrayList<String[]>();

        for (FileInfo fileInfo : fileList) {
        	File file = new File(dataRootPath + "/" + fileInfo.path);
            String title = file.getName();
            if (title.equals(".DS_Store")) {
            	continue;
            }
            // title = title.substring(0,
            //    title.length()-SUFFIX.length());
            String abs = IOUtil.getFileText(file);
            String[] pair = new String[2];
            pair[0] = title;
            pair[1] = abs;
            list.add(pair);
        }
        return list;
    }
}
