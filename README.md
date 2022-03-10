# Chat Application Android
+ Quick guide cho mọi người về project của bọn mình
+ Về cơ bản project sẽ sử dụng compileSdk 32 và jdk 8 => Không sửa để tránh lỗi version của các dependencies nhé !!
## Binding
+ Có sử dụng view binding để thuận tiện trong việc tương tác giữa view với code. Trong file build gradle chỉ cần thêm đoạn này vào để dùng binding.
```
 buildFeatures {
        viewBinding true
    }
```

+ Binding Usage: Khá đơn giản, ở màn hình activity tương ứng với mỗi view khai báo 1 đối tượng binding của activity đấy, ví dụ ở đây ở activity Sign-in sẽ khai báo và sử dụng như sau:

**Khai báo binding:**

```
private ActivitySignInBinding binding;

 @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
```

***Lưu ý ở phần setContentView thay vì sử dụng:***
```
setContentView(R.layout.Activity_name)
```
***Mình sẽ lấy trực tiếp content view từ thằng binding của activity đấy nhé*** 
```
setContentView(binding.getRoot()); 
```

**Cách dùng binding:**

Ví dụ bọn mày muốn lấy thuộc tính text của 1 cái textbox có name set ở view là inputUserName chẳng hạn thì viết như sau :
```
String userName = binding.inputUserName.getText().ToString()
// Hoặc set text cho nó
binding.inputUserName.setText("An tran ngu");
```
Nói chung là dùng binding nó ez vãi ca l đỡ phải lôi từng cái view ra !

## Firestore Database
+ **Lý thuyết** 

Một chút lí thuyết nếu mọi người lười đọc này => Firestore real time database nó là 1 dạng lưu trữ dữ liệu dạng NoQuery thần kì vcl, về cơ bản nó thao tác với dữ liệu qua tên collection và tên field thôi. Không có giàng buộc gì về kiểu dữ liệu hay quan hệ gì trong các collection nhé. Ví dụ 1 collection User hoàn toàn có thể có bản ghi có 2 trường hoặc 3 trường tùy trường hợp bọn m sử dụng.
+ **Set up => Dù mọi thứ đã setup oke rồi chỉ việc dùng thôi nhưng t nói qua các bước nhé:**

Step 1: Tạo project trên firebase đặt tên nó và copy cái id của project mình vào (trong build.gradle chỗ applicationId ý).

Step 2: Add cái file google-service.json nó cho khi tạo database vào trong thư mục project 

Step 3: Add dependency và plugin mà firebase yêu cầu vào trong build.gradle (hiện tại mình đang dùng phiên bản 24.0.1).

+ **Thao tác cơ bản trên firestore**

 **Cách khai báo database instance trong project :** Để dễ hình dung thì nó như cái data context của entity framework ý. Mình cần 1 cái instance của nó để có thể thao tác với cơ sở dữ liệu => cú pháp như sau:

```
FirebaseFirestore database = FirebaseFirestore.getInstance();
```
**Lưu một bản ghi mới:** Vì firestore lưu trữ dữ liệu theo kiểu string value nên mình sẽ lưu bản ghi mới bằng HashMap nhé. Ví dụ ở đây t muốn lưu 1 bản ghi gồm first_name và last_name vào Collection "users" chẳng hạn:

```
private void adDataToFireStore(){
        // Tạo instance nè
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // New ra 1 hash map để lưu user nè
        HashMap<String, Object> data = new HashMap<>();

        //Tạo ra 1 cặp key value với key là "first_name" value là "Nguyen" nè
        data.put("first_name","Nguyen");

        //Tạo ra 1 cặp key value với key là "last_name" value là "Hoang" nè
        data.put("last_name","Hoang");

        //Thực hiện add nè
        database.collection("users") //lấy ra cái collection muốn add data 
                .add(data) // add vào
                .addOnSuccessListener(documentReference -> {  //tạo ra 1 event khi hoàn tất việc add
            // Cái toast này như cái message box trong c# ý 
            Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(exception ->{   //event khi có lỗi trong quá trình add
            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
```

**Lấy ra bản ghi kèm điều kiện:** Giả dụ t muốn lấy 1 bản ghi của user để check login information có valid không đi:

```
 private void signIn(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        //Lấy ra tên collection của user quy định trong 1 file constants
        database.collection(Constants.KEY_COLLECTION_USERS)

                // Này giống LINQ này toàn thần đồng c# chắc k phải chỉ đâu nhỉ
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())

                // Hết điều kiện rồi thì lấy bản ghi ra thoai
                .get()

                // Không lỗi gì thì chạy event này 
                .addOnCompleteListener(task -> {

                    //Dòng này để check xem có bản ghi nào có username password phù hợp không
                    if(task.isSuccessful()  && task.getResult() != null && task.getResult().getDocuments().size() > 0){

                        // Các bản ghi trong firestore sẽ trả về dạng DocumentSnapshot nó cũng giống mấy cái data table trong sql ý
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);

                        //Sau khi lấy ra được bản ghi dưới dạng document snapshot thì thực hiện set vào object thôi
                       // Ở đây userName là tên của field
                       user.name = documentSnapshot.getString("userName");
                       ....          
                    }
                    else{
                        loading(false);
                        showToast("Unable to log in");
                    }
                });
    }

```

**Update bản ghi**: Oke thế muốn update thông tin user thì như lào? Logic vẫn như sql thoai -> Lấy ra id của bản ghi (ở đây là cái DocumentSnapshot id ý) -> Sau đó lấy các thuộc tính ra update ez:

```
 private void update(){
        //Map của đối tượng muốn update
        HashMap<String, String> entityToUpdate;
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        //Lấy ra tên collection của user quy định trong 1 file constants
        database.collection(Constants.KEY_COLLECTION_USERS)

                .document(Truyền id zô đây để lấy)

                // Tiến hành update thoai
                .update(entityToUpdate)

                // Không lỗi gì thì chạy event này 
                .addOnCompleteListener(task -> {                   
                       showToast("Update successfully");
                    }
                    else{
                        showToast("Unable to update");
                    }
                });
    }

```
**Delete bản ghi:** thực ra project của mình cũng không cần động gì đến xóa dữ liệu như cứ đọc qua cái code này nhớ đâu có ích... 

```
 private void delete(){
       
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        //Lấy ra tên collection của user quy định trong 1 file constants
        database.collection(Constants.KEY_COLLECTION_USERS)

                .document(Truyền id zô đây để xóa)

                // Tiến hành xóa thoai
                .delete()

                // Không lỗi gì thì chạy event này 
                .addOnCompleteListener(task -> {                   
                       showToast("Delete successfully");
                    }
                    else{
                        showToast("Unable to delete");
                    }
                });
    }

```

**Realtime update data:** Sau khi thao tác với data xong mà muốn UI được update ngay lập tức (realtime) thì firebase sinh ra 1 event gọi là SnapshotListener. Event này sẽ được trigger bất cứ khi nào có thay đổi trong 1 collection mà mình quy định được nghe và nó sẽ thực hiện update UI ngay sau đó.

**Demo 1 EventListenner dạng snapshot listenner:**

```
 //Định nghĩa 1 event listenner theo kiểu snapshotListenner
   private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
      if(error != null){
          return;
      }
        // value ở đây là dạng 1 query snapshot trong đó chứa 1 hoặc nhiều document snapshot
        //  trong trường hợp này mình sử dụng để loop qua tất cả các thay đổi vừa đc nghe thấy trong database

      if(value != null){
          int count = chatMessages.size();

          // loop qua tất cả các bản ghi có sự thay đổi

          for(DocumentChange documentChange : value.getDocumentChanges()){

              // Nếu bản ghi mới đc add vào thì thực hiện thêm mới vào list chat
              // ngoài ra kiểu documentChange còn có các type như modified hay removed tùy vào điều kiện mọi người muốn bắt

             if(documentChange.getType() == DocumentChange.Type.ADDED){

                 // đoạn này là lấy ra đoạn chat vừa đc thêm mới rồi lưu vào object thôi

                 ChatMessage chatMessage = new ChatMessage();
                 chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                 chatMessage.receiverID = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                 chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                 chatMessage.dateTime = getReadableDate(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                 chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);

                 //add vào list này
                 chatMessages.add(chatMessage);
             }
          }
          Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
          if(count == 0){
              chatAdapter.notifyDataSetChanged();
          }
          else{
              chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
              binding.chatRecycleView.smoothScrollToPosition(chatMessages.size() - 1);
          }
          binding.chatRecycleView.setVisibility(View.VISIBLE);
      }
      binding.progressBar.setVisibility(View.GONE);
      if(conversationId == null){
          checkForConversation();
      }
   });
```

**Sau khi tạo ra đc envent như trên rồi thì mình gắn nó vào collection muốn nhận update realtime thoai:**

```
 private void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManger.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receivedUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receivedUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManger.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

```


## SharedPreferences
Là 1 công cụ giúp chúng ta lưu data trong 1 file xml được chứa bên trong folder của application (DATA/data/[application package name]/shared_prefs/shared_preferences_name.xml) => file này sẽ nằm trong máy của user. 

Mục đích của nó như 1 cái session (nói thế cho dễ hình dung). Ví dụ với 1 webapp đi, khi người dùng login thì thường mình hay lưu user đấy vào session để có thẻ thao tác với dữ liệu của người dùng đấy đúng không? Trong app android nó cũng có phương thức đấy như nó sẽ lưu data dưới dạng key value trong 1 file xml.

Đơn giản như này nhé, khi 1 user login thành công ta sẽ là thao tác là đặt thông tin của người dùng đấy vào sharedPreference:

```
 if(loginSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));                     
                      }
```

Và lúc lấy data từ sharedPreference ra thì ta làm như sau:

```
      String id = preferenceManger.getString(Constants.KEY_USER_ID)
      String name = preferenceManger.getString(Constants.KEY_NAME)
      String  encodeImage = preferenceManger.getString(Constants.KEY_IMAGE)
```

## View, RecyclerView và Adapter

RecyclerView là một view group để hiện thị các view tương tự nhau. Để mọi người dễ hình dung hơn thì hãy tưởng tượng nó như 1 listview như WPF ý. Bên trong cái RecycleView thì luôn đi kèm trong đó là 1 cái **Adapter**. **Adapter** sẽ extends class **ViewHolder** và overide lại 3 phương thức chính đó là **onCreateViewHolder**, **onBindViewHolder** và **getItemCount**. Oke giải thích sơ qua từng thành phần nhé 

+ **Adapter**: Nó là công cụ sẽ handle tất cả phần việc của RecyclerView từ việc binding dữ liệu đến việc kết nối Datasource với các item. Một adapter được khởi tạo như sau: 
```
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> users;
    private final UserListener userListener;
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCotainerUserBinding itemCotainerUserBinding = ItemCotainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent, false
        );
        return new UserViewHolder(itemCotainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public UserAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    class UserViewHolder extends RecyclerView.ViewHolder{
        ItemCotainerUserBinding binding;

        UserViewHolder(ItemCotainerUserBinding itemCotainerUserBinding){
            super(itemCotainerUserBinding.getRoot());
            binding = itemCotainerUserBinding;
        }

        void setUserData(User user){
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }
    }

    private Bitmap getUserImage(String encodeImage){
        byte[] bytes = Base64.decode(encodeImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
```
Oke nhìn choáng vcl nhỉ @@, đây đọc qua giải thích từng component 1 này (chịu khó đọc docs với xem youtube về cái này nữa, nó nhiều vấn đề phết)

+ **ViewHolder là gì:** ViewHolder hiểu đơn giản là đại diện cho 1 thành phần trong một list được hiển thị trên RecyclerView. Ví dụ RecyclerView chứ 1 list các Students chẳng hạn thì 1 ViewHolder sẽ chứa thông tin display của 1 đối tượng student trong list đấy. Nó sẽ chịu trách nhiệm binding dữ liệu của từng phần tử trong list. 

+ **Phương thức onCreateViewHolder:** Như cái tên của nó => sử dụng để tạo ra viewholder với được inflate cái root layout của phần container view => Hỗ trợ việc binding trong ViewHolder.

+ **Phương thức onBindViewHolder:** Truy cập vào từng ViewHolder qua biến position (index của từng phần tử của list trong ViewHolder) để có thể tiến hành gán dữ liệu cũng như set các event listener cho mỗi phần tử ViewHolder trên RecyclerView đấy.

**Về cơ bản đấy là các bước cơ bản để có thể hiển thị ra 1 list object trên recycler view sử dụng adapter... :D**

***Mọi người chịu khó lên đọc docs và xem tutorial nhé nhiều lắm***


## Constants

Phần này sinh ra để quy định key name thống nhất cho các collection, field names và preference field name. Vì app này của mình đa số thao tác trên các tên key nên việc có một đống biến chuẩn thống nhất sẽ tránh việc gõ nhầm tên field blah blah => dễ lỗi ... 

## Signin-Signup-Logout logic
+ **Sign-up :** oke phần này chả có logic thực hiện quá phức tạp đơn giản là đọc các thông tin người dùng binding từ text box về rồi thực hiện lưu nó lên firestore thôi. Nhưng có cái phần sử lí lưu ảnh bằng Bitmap cũng khá thần kì để chia sẻ qua nhé: 

Tạo ra 1 event kiểu ActivityResultLauncher mục đích để có thể popub màn hình file explorer hoặc thư viện của người dùng để cho phép chọn ảnh. Sau đó thực hiện lấy uri của ảnh đó.
```
Uri imageUri = result.getData().getData();
```
 Sau khi lấy đc uri của ảnh rồi thì convert nó ra 1 InputStream 
```
 InputStream inputStream = getContentResolver().openInputStream(imageUri);
```
Từ InputStream vừa lấy đc convert qua Bitmapp thôi => Android studio hỗ trợ cho tận răng rồi

```
 Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
```
Sau khi có đc ảnh dạng bitmap thì có thể set vào avatar người dùng rồi. Nhưng mà khi lưu ảnh, chúng ta không thể lưu thẳng dạng bitmap lên trên database đc vì nó không hỗ trợ định dạng đấy => Lại lấy cái bitmap đấy ra rồi encode nó ra 1 string thôi, lúc lấy string đấy về thì decode ra lại bitmap ez:

```
private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
```

+ **Login :** Logic cũng rất đơn giản đầu tiên lấy thông tin input tài khoản mật khẩu về check như phần firestore trên t đã demo rồi đấy. Sau đó nếu tài khoản có tồn tại thì lưu thông tin người dùng vào SharedPreference để sang activity sau lấy ra dùng.
```
 if(task.isSuccessful()  && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
```
Tất nhiên khi bật app lên mình sẽ check trong sharedPreference trước xem nó còn lưu thông tin người dùng không => Nếu có thì redirect sang activity main luôn

+ **Log-out:** Xóa thông tin user trong SharedPreference để lần sau mở app không tự login và sau đó chuyển sang activity login thoai
```
 private void signOut(){
        showToast("Signing out");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnSuccessListener(unused -> {
            preferenceManager.clear();
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            finish();
        }).addOnFailureListener(e -> showToast("Error signing out"));

    }
```

## Chat logic
Comming-soon nhá
## Chat home-screen logic
Comming-soon nhá





