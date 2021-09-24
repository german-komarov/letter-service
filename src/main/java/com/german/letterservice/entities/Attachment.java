package com.german.letterservice.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.german.letterservice.exceptions.ConversionException;
import com.german.letterservice.util.Generators;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "attachments")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Attachment {


    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true,nullable = false,updatable = false,length = 50)
    private String uniqueCode;


    @Column(nullable = false, updatable = false)
    private String letterUniqueCode;


    @Column(nullable = false, updatable = false)
    private String originalFileName;

    @Column(updatable = false)
    private long fileSize;

    @Column(nullable = false, updatable = false)
    private String fileContentType;


    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinTable(name = "attachments_byte_contents",joinColumns = @JoinColumn(name = "attachment_id"),inverseJoinColumns = @JoinColumn(name = "byte_content_id"))
    private List<ByteContent> byteContent=new ArrayList<>(1);


    public Attachment(MultipartFile sourceFile,String letterUniqueCode) throws ConversionException {
        this.setUniqueCode(Generators.generateUniqueCode());
        this.setLetterUniqueCode(letterUniqueCode);
        this.setOriginalFileName(sourceFile.getOriginalFilename());
        this.setFileSize(sourceFile.getSize());
        this.setFileContentType(sourceFile.getContentType());

        byte[] bytes;
        try {
            bytes=sourceFile.getBytes();
        } catch (IOException e) {
            throw new ConversionException("IOException was raised during bytes extraction from multipart file");
        }


        this.setByteContent(Collections.singletonList(new ByteContent(bytes)));
    }
}
