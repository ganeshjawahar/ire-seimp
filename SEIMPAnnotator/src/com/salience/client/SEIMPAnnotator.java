package com.salience.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.salience.shared.SeimpTrainingRow;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class SEIMPAnnotator implements EntryPoint {

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final ServiceAsync proxyService = GWT
			.create(Service.class);

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		final VerticalPanel rootPanel=new VerticalPanel();
		rootPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		rootPanel.setSpacing(5);		
		rootPanel.setStyleName("style");
		rootPanel.setTitle("Main Page to do manual annotation of SEIMP dataset.");
		
		//Annotator Selection Panel
		final HorizontalPanel annotatorPanel=new HorizontalPanel();
		annotatorPanel.setSpacing(5);
		annotatorPanel.setTitle("Select the annotator (by name)");
		final Label annotatorLabel=new Label("Select annotator");
		annotatorLabel.setText("Select the annotator (by name)");
		annotatorPanel.add(annotatorLabel);
		final ListBox annotatorListBox=new ListBox();
		annotatorListBox.setTitle("Click here to get list of available annotators.");		
		for(final String name:ClientGlobals.ANNOTATOR_LIST)
			annotatorListBox.addItem(name);
		annotatorPanel.add(annotatorListBox);
		rootPanel.add(annotatorPanel);
		
		//Collection selection panel
		final HorizontalPanel collPanel=new HorizontalPanel();
		collPanel.setSpacing(5);
		collPanel.setTitle("Select the collection (by name)");
		final Label collLabel=new Label("Select collection");
		collLabel.setText("Select the collection (by name)");
		collPanel.add(collLabel);
		final ListBox collListBox=new ListBox();
		collListBox.setTitle("Click here to get list of available collections.");		
		for(final String db:ClientGlobals.COLLECTION_LIST)
			collListBox.addItem(db);
		collPanel.add(collListBox);
		rootPanel.add(collPanel);
		
		//TweetId
		final HorizontalPanel tweetPanel=new HorizontalPanel();
		tweetPanel.setTitle("Tweet id of Total");
		tweetPanel.setSpacing(5);
		final Label tweetPrefixLabel=new Label("Tweet");
		tweetPanel.add(tweetPrefixLabel);
		final TextBox tweetIdTB=new TextBox();
		tweetIdTB.setText("0");
		tweetIdTB.setWidth("30px");
		tweetPanel.add(tweetIdTB);
		final Label tweetSuffixLabel=new Label("of 0");
		tweetPanel.add(tweetSuffixLabel);
		//get the tweet count for the database
		proxyService.getTweetCount(ClientGlobals.MONGO_DB_NAME,collListBox.getItemText(collListBox.getSelectedIndex()),new BusyShowAsyncCallback<Integer>(new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) { }

			@Override
			public void onSuccess(Integer result) {
				if(result!=0) { 
					tweetIdTB.setText("1");
					tweetSuffixLabel.setText("of "+result);
				}
			}					
				
		}));
		final Button updateButton=new Button("update");
		updateButton.setTitle("Change to other tweet.");
		tweetPanel.add(updateButton);		
		rootPanel.add(tweetPanel);
		
		//Imp. button
		final HorizontalPanel buttonPanel=new HorizontalPanel();
		buttonPanel.setSpacing(5);
		final Button clearButton=new Button("Clear");
		clearButton.setTitle("Clear the manual annotation for the current tweet.");
		buttonPanel.add(clearButton);
		final Button submitButton=new Button("Submit");
		submitButton.setTitle("Save the manual annotation for the current tweet in db.");
		buttonPanel.add(submitButton);
		final Button prevButton=new Button("Prev");
		prevButton.setTitle("Go to the previous tweet, if possible");
		buttonPanel.add(prevButton);
		final Button nextButton=new Button("Next");
		nextButton.setTitle("Go to the next tweet, if possible");
		buttonPanel.add(nextButton);
		final Button checkButton=new Button("Check");
		checkButton.setTitle("Check un-annotated tweet indices.");
		buttonPanel.add(checkButton);
		rootPanel.add(buttonPanel);
		
		//Tweet content
		final VerticalPanel contentPanel=new VerticalPanel();
		contentPanel.setTitle("Main panel to select the annotations");
		contentPanel.setSpacing(5);
		contentPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		final Label tweetTextLabel=new Label("<tweet text>");
		tweetTextLabel.setTitle("Tweet text");
		contentPanel.add(tweetTextLabel);
		final HorizontalPanel imagePanel=new HorizontalPanel();
		imagePanel.setTitle("Set of images in the tweet.");
		contentPanel.add(imagePanel);
		final Label instLabel=new Label("Select only the NE's you see in the picture.");
		instLabel.setTitle("Annotation instruction");
		contentPanel.add(instLabel);
		final VerticalPanel nePanel=new VerticalPanel();
		contentPanel.setTitle("List of potential NE's for the tweet displayed.");
		contentPanel.setSpacing(5);
		contentPanel.add(nePanel);
		final HorizontalPanel commentsPanel=new HorizontalPanel();
		commentsPanel.setSpacing(5);
		final Label commentLabel=new Label("Comments");
		commentsPanel.add(commentLabel);
		final TextBox commentTB=new TextBox();
		commentTB.setWidth("150px");
		commentsPanel.add(commentTB);
		contentPanel.add(commentsPanel);
		final HorizontalPanel aPanel=new HorizontalPanel();
		contentPanel.add(aPanel);
		rootPanel.add(contentPanel);		
				
		final RowAbstract curRow=new RowAbstract();

		updateButton.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				//Check the entered tweet index is valid.
				final String tweetId=tweetIdTB.getText();
				final int maxIndex=Integer.parseInt(tweetSuffixLabel.getText().split(" ")[1]);
				if(!tweetId.matches("[0-9]+")) {
					Window.alert("Tweet index contains some non numerals.");
					return;
				}
				final int tIndex=Integer.parseInt(tweetId);				
				if(!(1<=tIndex && tIndex<=maxIndex)) {
					Window.alert("Invalid tweet range.");
					return;
				}
				
				//Get the tweet.
				proxyService.getTweet(ClientGlobals.MONGO_DB_NAME,collListBox.getItemText(collListBox.getSelectedIndex()),tIndex,new BusyShowAsyncCallback<SeimpTrainingRow>(new AsyncCallback<SeimpTrainingRow>(){
					@Override
					public void onFailure(Throwable caught) { }

					@Override
					public void onSuccess(SeimpTrainingRow result) {
						//Populate the tweet contents.
						if(result==null) {
							Window.alert("Tweet does not exist.");
							return;
						}
						
						//Set the tweet text
						tweetTextLabel.setText(result.getText());
						
						//Set the tweet images
						imagePanel.clear();
						for(final String imageUrl:result.getImageList()){
							final Image image=new Image(imageUrl);
							image.setSize("400px","250px");
							imagePanel.add(image);							
						}
						
						//Set the NE's
						nePanel.clear();
						if(result.getNeList()==null || result.getNeList().size()==0) {
							nePanel.add(new Label("NE list is empty."));
						} else {
							final List<String> sneList=result.getSneList();
							for(final String ne:result.getNeList()){
								final CheckBox neCB=new CheckBox(ne);
								neCB.setValue((sneList!=null && sneList.contains(ne))?true:false);
								nePanel.add(neCB);
							}
						}
						
						curRow.setRow(result);
						curRow.setCur(tIndex);
						tweetIdTB.setText(""+tIndex);
						aPanel.clear();
						
						//Set the annotator
						if(result.getAnnotator()!=null) {
							aPanel.add(new HTML("<font face='verdana' color='green'><b><i>Annotated already by "+result.getAnnotator().toUpperCase()+"</i></b></font>"));
						} else {
							aPanel.add(new HTML("<font face='verdana' color='red'><b><i>Annotated by NONE</i></b></font>"));
						}
						
						//Set the comments
						commentTB.setText((result.getComments()==null)?"":result.getComments());
						
					}					
						
				}));
				
			}
		});
		
		clearButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final SeimpTrainingRow row=curRow.getRow();
				if(row==null) {
					Window.alert("Current tweet is null.");
					return;
				}
				
				//Clear the annotations
				row.setAnnotator(null);
				row.setSneList(null);
				row.setComments(null);
				
				proxyService.saveSNE(ClientGlobals.MONGO_DB_NAME,collListBox.getItemText(collListBox.getSelectedIndex()),row,new BusyShowAsyncCallback<Void>(new AsyncCallback<Void>(){
					@Override
					public void onFailure(Throwable caught) { }

					@Override
					public void onSuccess(Void result) {
						Window.alert("Cleared this manual annotation.");
					}					
						
				}));
				
				//Clear the annotations
				if(nePanel.getWidgetCount()>0 && !(nePanel.getWidget(0) instanceof Label)) {
					for(int i=0;i<nePanel.getWidgetCount();i++){
						final CheckBox cbox=(CheckBox)nePanel.getWidget(i);
						if(cbox.getValue())
							cbox.setValue(false);
					}
				}
				
				aPanel.clear();
				aPanel.add(new HTML("<font face='verdana' color='red'><b><i>Annotated by NONE</i></b></font>"));
				
				//Clear the comments
				commentTB.setText("");
				
			}
			
		});
		
		submitButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final SeimpTrainingRow row=curRow.getRow();
				if(row==null) {
					Window.alert("Current tweet is null.");
					return;
				}
				
				//Save the annotator name
				row.setAnnotator(annotatorListBox.getItemText(annotatorListBox.getSelectedIndex()));
				
				//Set the comments
				row.setComments(commentTB.getText());
				
				//Save the annotations
				if(nePanel.getWidgetCount()>0 && !(nePanel.getWidget(0) instanceof Label)) {
					final List<String> sneList=new ArrayList<String>();
					for(int i=0;i<nePanel.getWidgetCount();i++){
						final CheckBox cbox=(CheckBox)nePanel.getWidget(i);
						if(cbox.getValue())
							sneList.add(cbox.getText());
					}
					if(sneList.size()>0) row.setSneList(sneList);
				}
				
				proxyService.saveSNE(ClientGlobals.MONGO_DB_NAME,collListBox.getItemText(collListBox.getSelectedIndex()),row,new BusyShowAsyncCallback<Void>(new AsyncCallback<Void>(){
					@Override
					public void onFailure(Throwable caught) { }

					@Override
					public void onSuccess(Void result) {
						Window.alert("Submitted successfully.");
					}					
						
				}));
				
				aPanel.clear();
				aPanel.add(new HTML("<font face='verdana' color='green'><b><i>Annotated already by "+row.getAnnotator().toUpperCase()+"</i></b></font>"));
				
			}
			
		});
		
		prevButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final int prev=curRow.getCur()-1;
				final int maxIndex=Integer.parseInt(tweetSuffixLabel.getText().split(" ")[1]);				
				if(!(1<=prev && prev<=maxIndex)) {
					Window.alert("Cannot go to previous tweet.");
					return;
				}				
				
				//Get the tweet.
				proxyService.getTweet(ClientGlobals.MONGO_DB_NAME,collListBox.getItemText(collListBox.getSelectedIndex()),prev,new BusyShowAsyncCallback<SeimpTrainingRow>(new AsyncCallback<SeimpTrainingRow>(){
					@Override
					public void onFailure(Throwable caught) { }

					@Override
					public void onSuccess(SeimpTrainingRow result) {
						//Populate the tweet contents.
						if(result==null) {
							Window.alert("Tweet does not exist.");
							return;
						}
						
						//Set the tweet text
						tweetTextLabel.setText(result.getText());
						
						//Set the tweet images
						imagePanel.clear();
						for(final String imageUrl:result.getImageList()){
							final Image image=new Image(imageUrl);
							image.setSize("400px","250px");
							imagePanel.add(image);							
						}
						
						//Set the NE's
						nePanel.clear();
						if(result.getNeList()==null || result.getNeList().size()==0) {
							nePanel.add(new Label("NE list is empty."));
						} else {
							final List<String> sneList=result.getSneList();
							for(final String ne:result.getNeList()){
								final CheckBox neCB=new CheckBox(ne);
								neCB.setValue((sneList!=null && sneList.contains(ne))?true:false);
								nePanel.add(neCB);
							}
						}
						
						curRow.setRow(result);
						curRow.setCur(prev);
						tweetIdTB.setText(""+prev);
						aPanel.clear();
						if(result.getAnnotator()!=null) {
							aPanel.add(new HTML("<font face='verdana' color='green'><b><i>Annotated already by "+result.getAnnotator().toUpperCase()+"</i></b></font>"));
						} else {
							aPanel.add(new HTML("<font face='verdana' color='red'><b><i>Annotated by NONE</i></b></font>"));
						}
						
						//Set the comments
						commentTB.setText((result.getComments()==null)?"":result.getComments());
					}					
						
				}));
				
			}
			
		});
		
		nextButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final int next=curRow.getCur()+1;
				final int maxIndex=Integer.parseInt(tweetSuffixLabel.getText().split(" ")[1]);				
				if(!(1<=next && next<=maxIndex)) {
					Window.alert("Cannot go to next tweet.");
					return;
				}				
				
				//Get the tweet.
				proxyService.getTweet(ClientGlobals.MONGO_DB_NAME,collListBox.getItemText(collListBox.getSelectedIndex()),next,new BusyShowAsyncCallback<SeimpTrainingRow>(new AsyncCallback<SeimpTrainingRow>(){
					@Override
					public void onFailure(Throwable caught) { }

					@Override
					public void onSuccess(SeimpTrainingRow result) {
						//Populate the tweet contents.
						if(result==null) {
							Window.alert("Tweet does not exist.");
							return;
						}
						
						//Set the tweet text
						tweetTextLabel.setText(result.getText());
						
						//Set the tweet images
						imagePanel.clear();
						for(final String imageUrl:result.getImageList()){
							final Image image=new Image(imageUrl);
							image.setSize("400px","250px");
							imagePanel.add(image);							
						}
						
						//Set the NE's
						nePanel.clear();
						if(result.getNeList()==null || result.getNeList().size()==0) {
							nePanel.add(new Label("NE list is empty."));
						} else {
							final List<String> sneList=result.getSneList();
							for(final String ne:result.getNeList()){
								final CheckBox neCB=new CheckBox(ne);
								neCB.setValue((sneList!=null && sneList.contains(ne))?true:false);
								nePanel.add(neCB);
							}
						}
						
						curRow.setRow(result);
						curRow.setCur(next);
						tweetIdTB.setText(""+next);
						aPanel.clear();
						if(result.getAnnotator()!=null) {
							aPanel.add(new HTML("<font face='verdana' color='green'><b><i>Annotated already by "+result.getAnnotator().toUpperCase()+"</i></b></font>"));
						} else {
							aPanel.add(new HTML("<font face='verdana' color='red'><b><i>Annotated by NONE</i></b></font>"));
						}
						
						//Set the comments
						commentTB.setText((result.getComments()==null)?"":result.getComments());
					}					
						
				}));
				
			}
			
		});
		
		checkButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				proxyService.getUnannotatedList(ClientGlobals.MONGO_DB_NAME,collListBox.getItemText(collListBox.getSelectedIndex()),new BusyShowAsyncCallback<List<String>>(new AsyncCallback<List<String>>(){
					@Override
					public void onFailure(Throwable caught) { }

					@Override
					public void onSuccess(List<String> result) {
						if(result==null || result.size()==0) {
							Window.alert("Nothing left to annotate. Enjoy!");
							return;
						}
						final StringBuffer buff=new StringBuffer();
						for(final String un:result)
							buff.append(un+",");
						Window.alert(buff.toString());
					}					
						
				}));
				
			}
			
		});
		
		
		RootPanel.get().add(rootPanel);
		
		
		/*
		proxyService.getTweetCount(ClientGlobals.MONGO_DB_NAME,"smallseimptrainingset",new BusyShowAsyncCallback<Integer>(new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) { }

			@Override
			public void onSuccess(Integer result) {
				Window.alert(""+result);				
			}					
				
		}));
		
		proxyService.getTweet(ClientGlobals.MONGO_DB_NAME,"smallseimptrainingset",1,new BusyShowAsyncCallback<SeimpTrainingRow>(new AsyncCallback<SeimpTrainingRow>(){
			@Override
			public void onFailure(Throwable caught) { }

			@Override
			public void onSuccess(SeimpTrainingRow result) {
				Window.alert(result.get_id()+"\t"+result.getText()+"\t"+result.getCreatedAt());				
			}					
				
		}));
		
		SeimpTrainingRow row=new SeimpTrainingRow();
		row.set_id(1);
		row.setSneList(Arrays.asList("1","2"));
		row.setAnnotator("g");

		proxyService.saveSNE(ClientGlobals.MONGO_DB_NAME,"test",row,new BusyShowAsyncCallback<Void>(new AsyncCallback<Void>(){
			@Override
			public void onFailure(Throwable caught) { }

			@Override
			public void onSuccess(Void result) {
				Window.alert("hello");
			}					
				
		}));
		
		proxyService.getUnannotatedList(ClientGlobals.MONGO_DB_NAME,"smallseimptrainingset",new BusyShowAsyncCallback<List<String>>(new AsyncCallback<List<String>>(){
			@Override
			public void onFailure(Throwable caught) { }

			@Override
			public void onSuccess(List<String> result) {
				String str="I-";
				for(String res:result)
					str+=res+"\t";
				Window.alert(str);
			}					
				
		}));
		*/
		
		
		
		
	}
}

class RowAbstract{
	SeimpTrainingRow row=null;
	int cur=0;

	public int getCur() {
		return cur;
	}

	public void setCur(int cur) {
		this.cur = cur;
	}

	public SeimpTrainingRow getRow() {
		return row;
	}

	public void setRow(SeimpTrainingRow row) {
		this.row = row;
	}
	
}
